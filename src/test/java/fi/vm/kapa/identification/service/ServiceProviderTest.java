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
import org.junit.Before;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MultivaluedHashMap;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ServiceProviderTest {

    @Mock
    ProxyClient proxyClient;
    @Mock
    PhaseIdService phaseIdInitSession;
    @Mock
    PhaseIdHistoryService historyService;
    @Mock
    UrlService urlService;
    @Mock
    SessionDataExtractor sessionDataExtractor;

    @Autowired
    @InjectMocks
    ServiceProvider serviceProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void updateSessionAndGetRedirectUriWithReplyAttackReturnsPhaseIdErrorUrl() throws Exception {
        when(historyService.areIdsConsumed(anyString(), anyString())).thenReturn(true);
        when(urlService.createPhaseIdErrorURL(anyString())).thenReturn(new URI("http://phase_id.error"));
        URI uri = serviceProvider.updateSessionAndGetRedirectUri(null, new MultivaluedHashMap<>(), "TID", "PID", "TAG");
        assertEquals("http://phase_id.error", uri.toString());
        verify(urlService).createPhaseIdErrorURL("TAG");
    }

    @Test
    public void updateSessionAndGetRedirectUriReturnsRequestErrorUriWhenSessionExtractorThrows() throws Exception {
        when(historyService.areIdsConsumed(anyString(), anyString())).thenReturn(false);
        when(phaseIdInitSession.validateTidAndPid(anyString(), anyString())).thenReturn(true);
        when(phaseIdInitSession.verifyPhaseId(anyString(), anyString(), anyString())).thenReturn(true);
        when(sessionDataExtractor.extractSessionData(any())).thenThrow(new InvalidIdentifierException());
        when(urlService.createRequestErrorURL(anyString())).thenReturn(new URI("http://request.error"));
        URI uri = serviceProvider.updateSessionAndGetRedirectUri(null, new MultivaluedHashMap<>(), "TID", "PID", "TAG");
        assertEquals("http://request.error", uri.toString());
        verify(urlService).createRequestErrorURL("TAG");
    }

    @Test
    public void updateSessionAndGetRedirectUriReturnsRequestErrorUriWhenUpdateSessionThrowsInternalError() throws Exception {
        when(historyService.areIdsConsumed(anyString(), anyString())).thenReturn(false);
        when(phaseIdInitSession.validateTidAndPid(anyString(), anyString())).thenReturn(true);
        when(phaseIdInitSession.verifyPhaseId(anyString(), anyString(), anyString())).thenReturn(true);
        when(sessionDataExtractor.extractSessionData(any())).thenReturn(Collections.emptyMap());
        when(urlService.createInternalErrorURL(anyString())).thenReturn(new URI("http://internal.error"));
        when(proxyClient.updateSession(anyMap(), anyString(), anyString(), anyString())).thenThrow(new InternalErrorException());
        URI uri = serviceProvider.updateSessionAndGetRedirectUri(null, new MultivaluedHashMap<>(), "TID", "PID", "TAG");
        assertEquals("http://internal.error", uri.toString());
        verify(urlService).createInternalErrorURL("TAG");
    }

    @Test
    public void updateSessionAndGetRedirectUriReturnsRequestErrorUriWhenUpdateSessionThrowsInvalidRequest() throws Exception {
        when(historyService.areIdsConsumed(anyString(), anyString())).thenReturn(false);
        when(phaseIdInitSession.validateTidAndPid(anyString(), anyString())).thenReturn(true);
        when(phaseIdInitSession.verifyPhaseId(anyString(), anyString(), anyString())).thenReturn(true);
        when(sessionDataExtractor.extractSessionData(any())).thenReturn(Collections.emptyMap());
        when(urlService.createRequestErrorURL(anyString())).thenReturn(new URI("http://request.error"));
        when(proxyClient.updateSession(anyMap(), anyString(), anyString(), anyString())).thenThrow(new InvalidRequestException());
        URI uri = serviceProvider.updateSessionAndGetRedirectUri(null, new MultivaluedHashMap<>(), "TID", "PID", "TAG");
        assertEquals("http://request.error", uri.toString());
        verify(urlService).createRequestErrorURL("TAG");
    }

    @Test
    public void updateSessionAndGetRedirectUri() throws Exception {
        when(historyService.areIdsConsumed(anyString(), anyString())).thenReturn(false);
        when(phaseIdInitSession.validateTidAndPid(anyString(), anyString())).thenReturn(true);
        when(phaseIdInitSession.verifyPhaseId(anyString(), anyString(), anyString())).thenReturn(true);
        HashMap<String,String> sessionData = new HashMap<>();
        when(sessionDataExtractor.extractSessionData(any())).thenReturn(sessionData);
        ProxyMessageDTO t = new ProxyMessageDTO();
        t.setTokenId("REPLY_TOKEN_ID");
        t.setPhaseId("REPLY_PHASE_ID");
        when(proxyClient.updateSession(anyMap(), anyString(), anyString(), anyString())).thenReturn(t);
        when(urlService.createSuccessURL(anyString(), anyString(), anyString())).thenReturn(new URI("http://success.com"));
        //
        URI uri = serviceProvider.updateSessionAndGetRedirectUri(null, new MultivaluedHashMap<>(), "TID", "PID", "TAG");
        assertEquals("http://success.com", uri.toString());
        verify(urlService).createSuccessURL("REPLY_TOKEN_ID", "REPLY_PHASE_ID", "TAG");
    }


}
