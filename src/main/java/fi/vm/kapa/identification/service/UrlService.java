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

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Constructs proper URI to be used when serivce provider encounters an authentication error.
 */
@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    @Value("${discovery.page.base.url}")
    private String discoveryPageBaseUrl;
    @Value("${failure.redirect}")
    private String failureRedirectBase;
    @Value("${success.redirect}")
    private String successRedirectBase;

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


    public URI generateErrorURI() {

        URI redirectURI = null;
        try {
            redirectURI = new URIBuilder(discoveryPageBaseUrl)
                    .addParameter("msg", "cancel")
                    .build();
        } catch (URISyntaxException e) {
            logger.error("Malformed sp error redirect url");
        }
        return redirectURI;
    }


    public URI createSuccessURL(String tid, String pid, String logTag) throws URISyntaxException {
        return new URIBuilder(successRedirectBase)
                .addParameter("tid", tid)
                .addParameter("pid", pid)
                .addParameter("tag", logTag)
                .build();
    }

    public URI createInternalErrorURL(String logTag) throws URISyntaxException {
        return new URIBuilder(failureRedirectBase)
                .addParameter("t", logTag)
                .addParameter("m", errorParamInternal)
                .build();
    }

    public URI createRequestErrorURL(String logTag) throws URISyntaxException {
        return new URIBuilder(failureRedirectBase)
                .addParameter("t", logTag)
                .addParameter("m", errorParamMessageInvalid)
                .build();
    }

    public URI createPhaseIdErrorURL(String logTag) throws URISyntaxException {
        return new URIBuilder(failureRedirectBase)
                .addParameter("t", logTag)
                .addParameter("m", errorParamPhaseId)
                .build();
    }

}
