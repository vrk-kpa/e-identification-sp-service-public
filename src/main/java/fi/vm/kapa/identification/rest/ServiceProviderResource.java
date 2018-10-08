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

import fi.vm.kapa.identification.service.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@Path("/saml")
public class ServiceProviderResource {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderResource.class);

    private final ServiceProvider serviceProvider;

    @Autowired
    public ServiceProviderResource(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @GET
    public Response processSAMLResponse(@Context HttpServletRequest request,
                                        @Context HttpHeaders headers,
                                        @QueryParam("tid") String tid,
                                        @QueryParam("pid") String pid,
                                        @QueryParam("tag") String logTag) {

        try {
            URI redirectUri = serviceProvider.updateSessionAndGetRedirectUri(request, headers.getRequestHeaders(),
                    tid, pid, logTag);
            return Response.status(Response.Status.FOUND).location(redirectUri).build();
        } catch (URISyntaxException e) {
            logger.warn("Invalid SP redirect URI {}", e.getInput());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.warn("Something went wrong: {}", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}

