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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ProxyClient {

    private static final Logger logger = LoggerFactory.getLogger(ProxyClient.class);

    private static final String HEADER_TYPE = "Content-Type";
    private static final String HEADER_VALUE = "application/json";
    public static final int HTTP_OK = 200;
    public static final int HTTP_INTERNAL_ERROR = 500;

    @Value("${proxy.rest.url}")
    private String proxyURLBase;

    public ProxyMessageDTO updateSession(Map<String, String> sessionData,
                                           String tid, String pid, String logTag) throws InternalErrorException, InvalidRequestException, IOException {

        String proxyCallUrl = proxyURLBase + tid + "&pid=" + pid;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod = new HttpPost(proxyCallUrl);
        postMethod.setHeader(HEADER_TYPE, HEADER_VALUE);

        Gson gson = new Gson();
        postMethod.setEntity(new StringEntity(gson.toJson(sessionData)));
        HttpContext context = HttpClientContext.create();

        CloseableHttpResponse restResponse = httpClient.execute(postMethod, context);

        ProxyMessageDTO messageDTO = null;

        int statusCode = restResponse.getStatusLine().getStatusCode();
        if (statusCode == HTTP_INTERNAL_ERROR) {
            logger.warn("<<{}>> Proxy encountered internal fault", logTag);
            throw new InternalErrorException();
        } else {
            if (statusCode == HTTP_OK) {
                messageDTO = gson.fromJson(
                        EntityUtils.toString(restResponse.getEntity()), ProxyMessageDTO.class);
            } else {
                logger.warn("<<{}>> Failed to build session, Proxy responded with HTTP {}", logTag, statusCode);
                throw new InvalidRequestException();

            }
        }
        restResponse.close();
        return messageDTO;
    }
}
