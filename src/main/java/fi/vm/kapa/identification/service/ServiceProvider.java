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
import fi.vm.kapa.identification.exception.InvalidIdentifierException;
import fi.vm.kapa.identification.exception.InvalidRequestException;
import fi.vm.kapa.identification.model.EidasSession;
import fi.vm.kapa.identification.model.EidasFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Component
public class ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AuditLog");

    private final PhaseIdService phaseIdInitSession;
    private final PhaseIdHistoryService historyService;
    private final ProxyClient proxyClient;
    private final UrlService urlService;
    private final SessionDataExtractor sessionDataExtractor;

    @Value("${phase.id.step.one}")
    private String stepSessionInit;
    @Value("${phase.id.step.two}")
    private String stepSessionBuild;


    @Autowired
    public ServiceProvider(ProxyClient proxyClient,
                           @Named("sessionInitPhaseIdService") PhaseIdService phaseIdInitSession,
                           PhaseIdHistoryService historyService, UrlService urlService,
                           SessionDataExtractor sessionDataExtractor) {
        this.proxyClient = proxyClient;
        this.historyService = historyService;
        this.phaseIdInitSession = phaseIdInitSession;
        this.urlService = urlService;
        this.sessionDataExtractor = sessionDataExtractor;
    }

    public URI updateSessionAndGetRedirectUri(HttpServletRequest request, MultivaluedMap<String, String> headers,
                                              String tid, String pid, String logTag) throws URISyntaxException {

        logger.debug("Processing SAML response with tid {} and pid {}", tid, pid);
        URI redirectUrl;
        EidasFlow eidasFlow;
        try {
            /* Token and phase IDs must be checked if they've been used already
             * in order to prevent replay attacks, there's only a small history
             * that needs to be checked so that performance isn't penalized
             */
            boolean tidAndPidAreValid = validateTidAndPid(tid, pid, logTag);
            logger.debug("Got inbound SAML2 message, request token ID: {}, request phase ID: {}", tid, pid);
            if (tidAndPidAreValid) {
                Map<String,String> sessionData = sessionDataExtractor.extractSessionData(headers);
                ProxyMessageDTO message = updateSession(tid, logTag, sessionData);
                if ( message.getEidasContactAddress() != null && request != null ) {
                    eidasFlow = EidasFlow.EIDAS_FORM;
                    HttpSession session = request.getSession(true);
                    EidasSession eidasSession = new EidasSession();
                    eidasSession.setEidasContactAddress(message.getEidasContactAddress());
                    eidasSession.setDisplayNameFI(message.getDisplayNameFI());
                    eidasSession.setEntityID(message.getEntityId());
                    eidasSession.setLogTag(logTag);
                    session.setAttribute(EidasSession.SESSION_ATTRIBUTE, eidasSession);
                    redirectUrl = urlService.createEidasFormUrl(message.getEntityId(), logTag);
                }
                else {
                    eidasFlow = EidasFlow.DEFAULT;
                    redirectUrl = urlService.createSuccessURL(message.getTokenId(), message.getPhaseId(), logTag);
                }
                logger.debug("Redirect URL to IdP: " + redirectUrl.toString());
                AuditLogRowBuilder auditLogRowBuilder = new AuditLogRowBuilder(sessionData, message, logTag, eidasFlow);
                auditLogger.info(auditLogRowBuilder.build());
            } else {
                logger.warn("<<{}>> Got invalid phase ID", logTag);
                redirectUrl = urlService.createPhaseIdErrorURL(logTag);
            }
        } catch (InvalidIdentifierException e) {
            logger.warn("<<{}>> No identifier found from SAML message", logTag, e);
            redirectUrl = urlService.createRequestErrorURL(logTag);
        } catch (InternalErrorException e) {
            logger.warn("<<{}>> Internal fault occurred connection to proxy", logTag, e);
            redirectUrl = urlService.createInternalErrorURL(logTag);
        } catch (InvalidRequestException e) {
            logger.warn("<<{}>> Invalid request occurred connection to proxy", logTag, e);
            redirectUrl = urlService.createRequestErrorURL(logTag);
        } catch (IOException e) {
            logger.warn("<<{}>> Failed to connect proxy", logTag, e);
            redirectUrl = urlService.createInternalErrorURL(logTag);
        }

        return redirectUrl;
    }

    boolean validateTidAndPid(String tid, String pid, String logTag) {
        if (!historyService.areIdsConsumed(tid, pid)) {
            try {
                /* Both token ID and phase ID values must always match to a given set of rules
                 * since these values are exposed to public, they could have been tampered
                 */
                return phaseIdInitSession.validateTidAndPid(tid, pid) &&
                        phaseIdInitSession.verifyPhaseId(pid, tid, stepSessionInit);
            } catch (Exception e) {
                logger.warn("<<{}>> Failed to verify or generate next phase ID", logTag, e);
            }
        } else {
            logger.warn("Received already consumed token and phase IDs!!");
        }
        return false;
    }

    ProxyMessageDTO updateSession(String tid, String logTag, Map<String,String> sessionData) throws InternalErrorException, InvalidRequestException, IOException {
        /* There must be an identifier in the SAML message, since only trusted IdPs are accepted
         * and they always supply a proper identifier value that can be mapped to a specific value
         */
        String newPhaseID = phaseIdInitSession.newPhaseId(tid, stepSessionBuild);
        logger.debug("--> next phase ID: {}", newPhaseID);
        return proxyClient.updateSession(sessionData, tid, newPhaseID, logTag);
    }

}
