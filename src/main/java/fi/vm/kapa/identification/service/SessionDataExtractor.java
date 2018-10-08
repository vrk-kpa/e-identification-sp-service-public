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

import fi.vm.kapa.identification.exception.InvalidIdentifierException;
import fi.vm.kapa.identification.type.Identifier;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

import static fi.vm.kapa.identification.type.Identifier.Types.*;

@Component
public class SessionDataExtractor {
    private static final String SP_ATTRIBUTE_PREFIX = "AJP_";

    /* Do not reorder, because this affects mobile authentication.
     * These values are fetched from shibboleth2.xml and these must be
     * placed in the same order as the enum in Identifier.Types.
     */
    private static final Identifier.Types[] identifierTypePriority = {SATU, HETU, KID, EIDAS_ID};
    private Map<Identifier.Types,String> enumToNameMap = ImmutableMap.<Identifier.Types,String>builder()
            .put(SATU, "AJP_satu")
            .put(HETU, "AJP_hetu")
            .put(KID, "AJP_tfiKid")
            .put(EIDAS_ID, "AJP_eidasPersonIdentifier")
            .build();


    public Map<String,String> extractSessionData(MultivaluedMap<String,String> headers) throws InvalidIdentifierException {
        Map<String,String> sessionData = extractRelevantHeaders(headers);
        Identifier.Types identifierType = getIdentifierType(sessionData);
        sessionData.put(Identifier.typeKey, identifierType.name());
        return sessionData;
    }


    Identifier.Types getIdentifierType(Map<String,String> sessionData) throws InvalidIdentifierException {
        if ("urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorContract".equals(sessionData.get("AJP_Shib-Authentication-Method"))) {
            //Set identifierType to HETU if mobile authenticator has been used. Will be removed once mobile cert issuer is available.
            return HETU;
        } else {
            for (Identifier.Types type : identifierTypePriority) {
                if (StringUtils.isNotBlank(sessionData.get(enumToNameMap.get(type)))) {
                    return type;
                }
            }
            throw new InvalidIdentifierException();
        }
    }


    Map<String,String> extractRelevantHeaders(MultivaluedMap<String,String> headers) {
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

}
