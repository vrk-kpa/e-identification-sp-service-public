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
import fi.vm.kapa.identification.dto.ProxyMessageDTO;
import fi.vm.kapa.identification.exception.InternalErrorException;
import fi.vm.kapa.identification.exception.InvalidRequestException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

@Component
public class ProxyClient {

    private static final Logger logger = LoggerFactory.getLogger(ProxyClient.class);

    private static final String HEADER_TYPE = "Content-Type";
    private static final String HEADER_VALUE = "application/json";

    private final String proxyURLBase;

    private final CloseableHttpClient httpClient;

    public ProxyClient(String proxyURLBase) {
        this(proxyURLBase, HttpClients.createDefault());
    }

    @Autowired
    public ProxyClient(@Value("${proxy.rest.url}") String proxyURLBase, CloseableHttpClient client) {
        logger.debug("ProxyClient constructed with url {} and client {}", proxyURLBase, client);
        this.proxyURLBase = proxyURLBase;
        this.httpClient = client;
    }

    public ProxyMessageDTO updateSession(Map<String,String> sessionData,
                                         String tid, String pid, String logTag) throws InternalErrorException, InvalidRequestException, IOException  {

        Gson gson = new Gson();

        URI proxyUpdateUri = UriBuilder.fromUri(proxyURLBase)
                .queryParam("tid", tid)
                .queryParam("pid", pid)
                .queryParam("tag", logTag)
                .build();
        HttpUriRequest postMethod = RequestBuilder.post()
                .setUri(proxyUpdateUri)
                .setHeader(HEADER_TYPE, HEADER_VALUE)
                .setEntity(new StringEntity(gson.toJson(sessionData)))
                .build();
        HttpContext context = HttpClientContext.create();

        ResponseHandler<ProxyMessageDTO> rh = response -> {
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
            ContentType contentType = ContentType.getOrDefault(entity);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(entity.getContent(), contentType.getCharset()));
            try {
                return gson.fromJson(in, ProxyMessageDTO.class);
            } finally {
                in.close();
            }
        };


        try {
            return httpClient.execute(postMethod, rh, context);
        } catch (HttpResponseException e) {
            logger.warn("Got status code {} from proxy", e.getStatusCode(), e);
            if (e.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw new InternalErrorException();
            } else {
                throw new InvalidRequestException();
            }
        }

    }
}
