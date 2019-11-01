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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;

@Component
@Path("/sp-error")
public class ErrorResource {

    private static final Logger logger = LoggerFactory.getLogger(ErrorResource.class);

    private UrlService urlService;

    @Autowired
    public ErrorResource(UrlService urlService) {
        this.urlService = urlService;
    }

    @GET
    public Response getErrorLocation(@QueryParam("RelayState") String relayState,
            @Context HttpHeaders headers) {
        Map<String, Cookie> cookies = headers.getCookies();
        ArrayList<NewCookie> addCookies = new ArrayList<>();
        if (cookies != null) {
            for(Entry<String, Cookie> e : cookies.entrySet()) {   
                NewCookie cookie = new NewCookie(e.getValue());
                if (cookie.getName().startsWith("_shibstate_")) {
                    addCookies.add( new NewCookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain(), 
                            cookie.getVersion(), cookie.getComment(), 0, cookie.getExpiry(), 
                            cookie.isSecure(), cookie.isHttpOnly()));
                } else {
                    addCookies.add(new NewCookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain(), 
                            cookie.getVersion(), cookie.getComment(), cookie.getMaxAge(), cookie.getExpiry(), 
                            cookie.isSecure(), cookie.isHttpOnly()));
                }
            }
        }
        URI redirectURI = urlService.generateErrorURI(relayState);
        return Response.status(Response.Status.FOUND).location(redirectURI).cookie(addCookies.toArray(new NewCookie[0])).build();        
    }
}
