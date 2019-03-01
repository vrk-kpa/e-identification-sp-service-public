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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UrlServiceTest {

    private UrlService urlService;

    @Before
    public void init() {
        urlService = new UrlService();
    }

    @Test
    public void testGenerateErrorUrl() {
        ReflectionTestUtils.setField(urlService, "cancelRedirectBase", "http://test.page/");
        URI errorUri = urlService.generateErrorURI("https://www.tunnistus-dev.xyz/sp-secured?&tid=9ncokgspt1mm2b6538irksao6t&pid=33ed9752ae0b80dce2dee5882962212aac20bdf20f01d33a2703535f0584b499&tag=18050313023c24&conversation=e1s1");
        assertNotNull(errorUri);
        assertEquals("http://test.page/?conversation=e1s1&tid=9ncokgspt1mm2b6538irksao6t&pid=33ed9752ae0b80dce2dee5882962212aac20bdf20f01d33a2703535f0584b499&tag=18050313023c24&status=returnFromIdp",
                errorUri.toString());
    }

    @Test
    public void createSuccessUrl() throws Exception {
        ReflectionTestUtils.setField(urlService, "successRedirectBase", "http://success");
        URI successUrl = urlService.createSuccessURL("TID", "PID", "TAG");
        assertEquals("http://success/?tid=TID&pid=PID&tag=TAG", successUrl.toString());
    }

    @Test
    public void createInternalErrorUrl() throws Exception {
        ReflectionTestUtils.setField(urlService, "failureRedirectBase", "http://failure");
        ReflectionTestUtils.setField(urlService, "errorParamInternal", "INTERNAL");
        URI internalErrorUrl = urlService.createInternalErrorURL("TAG");
        assertEquals("http://failure/?t=TAG&m=INTERNAL", internalErrorUrl.toString());
    }

    @Test
    public void createRequestErrorUrl() throws Exception {
        ReflectionTestUtils.setField(urlService, "failureRedirectBase", "http://failure");
        ReflectionTestUtils.setField(urlService, "errorParamMessageInvalid", "REQUEST");
        URI requestErrorUrl = urlService.createRequestErrorURL("TAG");
        assertEquals("http://failure/?t=TAG&m=REQUEST", requestErrorUrl.toString());
    }

    @Test
    public void createPhaseIdErrorUrl() throws Exception {
        ReflectionTestUtils.setField(urlService, "failureRedirectBase", "http://failure");
        ReflectionTestUtils.setField(urlService, "errorParamPhaseId", "PHASE_ID");
        URI phaseIdErrorUrl = urlService.createPhaseIdErrorURL("TAG");
        assertEquals("http://failure/?t=TAG&m=PHASE_ID", phaseIdErrorUrl.toString());
    }

}
