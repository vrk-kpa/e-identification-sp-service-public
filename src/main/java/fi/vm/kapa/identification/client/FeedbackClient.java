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
package fi.vm.kapa.identification.client;

import com.google.gson.Gson;
import fi.vm.kapa.identification.exception.InternalErrorException;
import fi.vm.kapa.identification.exception.InvalidRequestException;
import fi.vm.kapa.identification.model.EidasContactForm;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

@Component
public class FeedbackClient {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackClient.class);

    private final String feedbackEidasURLBase;

    private final CloseableHttpClient httpClient;

    public FeedbackClient(String feedbackEidasURLBase) {
        this(feedbackEidasURLBase, HttpClients.createDefault());
    }

    @Autowired
    public FeedbackClient(@Value("${eidas.email.post.url}") String feedbackEidasURLBase, CloseableHttpClient client) {
        logger.debug("FeedbackClient constructed with url {} and client {}", feedbackEidasURLBase, client);
        this.feedbackEidasURLBase = feedbackEidasURLBase;
        this.httpClient = client;
    }

    public HttpResponse postData(EidasContactForm eidasFormData) throws InternalErrorException, InvalidRequestException, IOException  {

        Gson gson = new Gson();

        URI eidasPostUri = UriBuilder.fromUri(feedbackEidasURLBase).build();
        HttpUriRequest postMethod = RequestBuilder.post()
                .setUri(eidasPostUri)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setEntity(new StringEntity(gson.toJson(eidasFormData), "UTF-8"))
                .setCharset(Charset.forName("UTF-8"))
                .build();
        HttpContext context = HttpClientContext.create();

        ResponseHandler<HttpResponse> rh = response -> {
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            if (entity == null) {
                throw new ClientProtocolException("Response contains no content");
            }
            return response;
        };


        try {
            return httpClient.execute(postMethod, rh, context);
        } catch (HttpResponseException e) {
            logger.warn("Got status code {} from feedback", e.getStatusCode(), e);
            if (e.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw new InternalErrorException();
            } else {
                throw new InvalidRequestException();
            }
        }

    }
}
