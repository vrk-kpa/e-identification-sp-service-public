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
package fi.vm.kapa.identification.rest;

import fi.vm.kapa.identification.service.UrlService;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ErrorResourceTest {

    private ErrorResource resource;
    
    @Mock
    private HttpHeaders httpHeaders;

    @Before
    public void init() throws URISyntaxException {
        UrlService urlService = mock(UrlService.class);
        when(urlService.generateErrorURI("")).thenReturn(new URI("http://test.error.fi"));
        resource = new ErrorResource(urlService);
        httpHeaders = mock(HttpHeaders.class);
    }

    @Test
    public void testGetErrorLocation() {
        Map<String, Cookie> cookies = new HashMap<>();       
        when(httpHeaders.getCookies()).thenReturn(cookies);
        Response response = resource.getErrorLocation("", httpHeaders);
        assertNotNull(response);
        assertEquals(Response.Status.FOUND.getStatusCode(), response.getStatus());
        assertEquals("http://test.error.fi", response.getLocation().toString());
    }
    
    @Test
    public void testShibstateCookieRemoval() {
        Map<String, Cookie> cookies = new HashMap<>();
        cookies.put("_shibstate_12471946", new NewCookie(new Cookie("_shibstate_12471946", "value"), "", 10, false));
        when(httpHeaders.getCookies()).thenReturn(cookies);
        Response response = resource.getErrorLocation("", httpHeaders);
        response.getCookies().entrySet().stream().forEach((entry) -> {
            assert (entry.getValue().getName().startsWith("_shibstate_"));
            assertEquals(0, entry.getValue().getMaxAge());
        });
    }

    @Test
    public void testOtherCookiesIntact() {
        Map<String, Cookie> cookies = new HashMap<>();
        NewCookie cookie = new NewCookie("not_shib_state", "value", "/", "", 1, "", 10000, new Date(System.currentTimeMillis() + 100000), false, false);
        cookies.put("not_shibstate_cookie", cookie.toCookie());
        when(httpHeaders.getCookies()).thenReturn(cookies);
        Response response = resource.getErrorLocation("", httpHeaders);
        assert (!response.getCookies().isEmpty());
        response.getCookies().entrySet().stream().forEach((entry) -> {
            assertEquals("not_shib_state", entry.getValue().getName());    
            assertEquals("value", entry.getValue().getValue());
        });
    }
}
