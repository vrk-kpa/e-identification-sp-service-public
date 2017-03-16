/**
 * The MIT License
 * Copyright (c) 2015 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vm.kapa.identification.service;

import fi.vm.kapa.identification.client.ProxyClient;
import fi.vm.kapa.identification.dto.ProxyMessageDTO;
import fi.vm.kapa.identification.exception.InternalErrorException;
import fi.vm.kapa.identification.exception.InvalidRequestException;
import fi.vm.kapa.identification.type.Identifier;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static fi.vm.kapa.identification.type.Identifier.Types.*;

@Component
public class ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);
    private static final String SP_ATTRIBUTE_PREFIX = "AJP_";

    private PhaseIdService phaseIdInitSession;
    private PhaseIdHistoryService historyService;
    private ProxyClient proxyClient;

    @Value("${success.redirect}")
    private String successRedirectBase;
    @Value("${failure.redirect}")
    private String failureRedirectBase;
    @Value("${phase.id.step.one}")
    private String stepSessionInit;
    @Value("${phase.id.step.two}")
    private String stepSessionBuild;

    /* These strings define the error redirect URL query parameter that can be
     * used to guide the error page, the value matches the property langId that
     * fetches the correct language variant for the error message
     */
    @Value("${failure.param.internal}")
    private String errorParamInternal;
    @Value("${failure.param.phaseid}")
    private String errorParamPhaseId;
    @Value("${failure.param.invalid}")
    private String errorParamMessageInvalid;

    /* Do not reorder, because this affects mobile authentication.
     * These values are fetched from shibboleth2.xml and these must be
     * placed in the same order as the enum in Identifier.Types.
     */
    private static final Identifier.Types[] identifierTypePriority = {SATU, HETU, KID, EPPN, UID, EIDAS_ID};
    private Map<Identifier.Types,String> enumToNameMap = ImmutableMap.<Identifier.Types,String>builder()
            .put(SATU, "AJP_satu")
            .put(HETU, "AJP_hetu")
            .put(KID, "AJP_tfiKid")
            .put(EPPN, "AJP_eppn")
            .put(UID, "AJP_uid")
            .put(EIDAS_ID, "AJP_eidasPersonIdentifier")
            .build();

    @Autowired
    public ServiceProvider(ProxyClient proxyClient,
                           @Named("sessionInitPhaseIdService") PhaseIdService phaseIdInitSession) {
        this.proxyClient = proxyClient;
        this.historyService = PhaseIdHistoryService.getInstance();
        this.phaseIdInitSession = phaseIdInitSession;
    }

    public String processSAMLResponse(MultivaluedMap<String,String> headers,
                                      String tid, String pid, String logTag) {

        logger.debug("Processing SAML response with tid {} and pid {}", tid, pid);
        String newPhaseID = null;
        String redirectUrl = null;

        /* Token and phase IDs must be checked if they've been used already
         * in order to prevent replay attacks, there's only a small history
         * that needs to be checked so that performance isn't penalized
         */
        if (!historyService.areIdsConsumed(tid, pid)) {
            try {
                /* Both token ID and phase ID values must always match to a given set of rules
                 * since these values are exposed to public, they could have been tampered
                 */
                if (phaseIdInitSession.validateTidAndPid(tid, pid) &&
                        phaseIdInitSession.verifyPhaseId(pid, tid, stepSessionInit)) {
                    newPhaseID = phaseIdInitSession.newPhaseId(tid, stepSessionBuild);
                }
            } catch (Exception e) {
                logger.warn("<<{}>> Failed to verify or generate next phase ID", logTag, e);
            }
        } else {
            logger.warn("Received already consumed token and phase IDs!!");
        }

        logger.debug("Got inbound SAML2 message, request token ID: {}, request phase ID: {}", tid, pid);
        logger.debug("--> next phase ID: {}", newPhaseID);

        if (newPhaseID != null) {
            Map<String,String> sessionData = extractSessionData(headers);

            /* There must be an identifier in the SAML message, since only trusted IdPs are accepted
             * and they always supply a proper identifier value that can be mapped to a specific value
             */
            
            Identifier.Types identifierType;
            if ("urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorContract".equals(sessionData.get("AJP_Shib-Authentication-Method"))) {
                //Set identifierType to HETU if mobile authenticator has been used. Will be removed once mobile cert issuer is available.
                identifierType = HETU;
            } else {
                identifierType = getIdentifierType(sessionData);
            }
            if (isValidIdentifierType(identifierType)) {
                sessionData.put(Identifier.typeKey, "" + identifierType);


                try {
                    ProxyMessageDTO message = proxyClient.updateSession(sessionData, tid, newPhaseID, logTag);
                    String pidFromProxy = message.getPhaseId();
                    String tidFromProxy = message.getTokenId();

                    String idpCallUrl = successRedirectBase + "?tid=" + tidFromProxy + "&pid=" + pidFromProxy + "&tag=" + logTag;
                    logger.debug("Redirect URL to IdP: " + idpCallUrl);

                    redirectUrl = idpCallUrl;
                } catch (InternalErrorException e) {
                    logger.warn("<<{}>> Internal fault occurred connection to proxy", logTag, e);
                    redirectUrl = createErrorURL(logTag, errorParamInternal);
                } catch (InvalidRequestException e) {
                    logger.warn("<<{}>> Invalid request occurred connection to proxy", logTag, e);
                    redirectUrl = createErrorURL(logTag, errorParamMessageInvalid);
                } catch (IOException e) {
                    logger.warn("<<{}>> Failed to connect proxy", logTag, e);
                    redirectUrl = createErrorURL(logTag, errorParamInternal);
                }

            } else {
                logger.warn("<<{}>> No identifier found from SAML message", logTag);

                redirectUrl = createErrorURL(logTag, errorParamMessageInvalid);
            }
        } else {
            logger.warn("<<{}>> Got invalid phase ID", logTag);

            redirectUrl = createErrorURL(logTag, errorParamPhaseId);
        }
        return redirectUrl;
    }

    boolean isValidIdentifierType(Identifier.Types identifierType) {
        return identifierType != null;
    }

    Identifier.Types getIdentifierType(Map<String,String> sessionData) {
        for (Identifier.Types type: identifierTypePriority) {
            if (StringUtils.isNotBlank(sessionData.get(enumToNameMap.get(type)))) {
                return type;
            }
        }
        return null;
    }

    Map<String,String> extractSessionData(MultivaluedMap<String,String> headers) {
        Map<String,String> filteredHeaderData = new HashMap<>();
        headers.keySet()
                .stream()
                .filter(headerName -> headerName.startsWith(SP_ATTRIBUTE_PREFIX) || "REMOTE_USER".equals(headerName))
                .forEach(headerName -> {
                    String headerValue = headers.getFirst(headerName);
                    if (StringUtils.isNotBlank(headerValue)) {
                        filteredHeaderData.put(headerName, headerValue);
                    }
                });
        return filteredHeaderData;
    }

    private String createErrorURL(String logTag, String message) {
        return failureRedirectBase + "?t=" + logTag + "&m=" + message;
    }
}
