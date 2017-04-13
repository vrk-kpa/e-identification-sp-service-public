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

import fi.vm.kapa.identification.type.Identifier;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ServiceProviderSecurityTest {

    /**
     * ServiceProvider must not allow injected HTTP headers to be delivered to
     * proxy.
     * SP takes care of anything starting with prefix defined in
     * 'attributePrefix' attribute of <ApplicationDefaults /> in file
     * shibboleth2.xml and does not allow injecting matching headers.
     *
     * @throws Exception
     */
    @Test
    public void checkAndParseIdentifierAndDataDoesNotUseIdentifierTypeFromHeaders() throws Exception {
        MultivaluedMap<String,String> headers = new MultivaluedHashMap<>();
        headers.putSingle("AJP_satu", "TEST_SATU");
        headers.putSingle(Identifier.typeKey, "BUSTED_BY_HEADER_INJECTION");
        SessionDataExtractor extractor = new SessionDataExtractor();
        Map<String,String> sessionData =  extractor.extractSessionData(headers);
        assertThat(sessionData.get(Identifier.typeKey), is(not("BUSTED_BY_HEADER_INJECTION")));
    }
}