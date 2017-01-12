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

import org.junit.Test;

import fi.vm.kapa.identification.type.Identifier;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class ServiceProviderTest {

    @Test
    public void checkAndParseIdentifierAndDataReturnsFalseWhenNoIdentifierFoundInSessionData() throws Exception {
        ServiceProvider sp = new ServiceProvider(null, null);
        HashMap<String,String> sessionData = new HashMap<>();
        sessionData.put("AJP_notIdentifier", "SHOULD_NOT_BE_ACCEPTED_AS_IDENTIFIER");
        assertFalse(sp.isValidIdentifierType(sp.getIdentifierType(sessionData)));
    }

    @Test
    public void checkAndParseIdentifierAndDataReturnsTrueWhenHetuIdentifierFoundInSessionData() throws Exception {
        ServiceProvider sp = new ServiceProvider(null, null);
        HashMap<String,String> sessionData = new HashMap<>();
        sessionData.put("AJP_hetu", "TEST_HETU");
        assertTrue(sp.isValidIdentifierType(sp.getIdentifierType(sessionData)));
    }

    @Test
    public void getIdentifierTypeReturnsHetuWhenHetuAndUidIdentifierFoundInSessionData() throws Exception {
        ServiceProvider sp = new ServiceProvider(null, null);
        HashMap<String,String> sessionData = new HashMap<>();
        sessionData.put("AJP_hetu", "TEST_HETU");
        sessionData.put("AJP_uid", "TEST_UID");
        assertTrue(sp.getIdentifierType(sessionData) == Identifier.Types.HETU);
    }

    @Test
    public void getIdentifierTypeReturnsSatuForMobileIdpWhenHetuAndSatuIdentifierFoundInSessionData() throws Exception {
        ServiceProvider sp = new ServiceProvider(null, null);
        HashMap<String,String> sessionData = new HashMap<>();
        sessionData.put("AJP_hetu", "TEST_HETU");
        sessionData.put("AJP_satu", "TEST_SATU");
        assertTrue(sp.getIdentifierType(sessionData) == Identifier.Types.SATU);
    }

    @Test
    public void checkAndParseIdentifierAndDataReturnsTrueWhenSatuIdentifierFoundInSessionData() throws Exception {
        ServiceProvider sp = new ServiceProvider(null, null);
        HashMap<String,String> sessionData = new HashMap<>();
        sessionData.put("AJP_satu", "TEST_SATU");
        assertTrue(sp.isValidIdentifierType(sp.getIdentifierType(sessionData)));
    }

    @Test
    public void checkAndParseIdentifierAndDataReturnsTrueWhenKidIdentifierFoundInSessionData() throws Exception {
        ServiceProvider sp = new ServiceProvider(null, null);
        HashMap<String,String> sessionData = new HashMap<>();
        sessionData.put("AJP_tfiKid", "TEST_KID");
        assertTrue(sp.isValidIdentifierType(sp.getIdentifierType(sessionData)));
    }

    @Test
    public void checkAndParseIdentifierAndDataReturnsTrueWhenEppnIdentifierFoundInSessionData() throws Exception {
        ServiceProvider sp = new ServiceProvider(null, null);
        HashMap<String,String> sessionData = new HashMap<>();
        sessionData.put("AJP_eppn", "TEST_EPPN");
        assertTrue(sp.isValidIdentifierType(sp.getIdentifierType(sessionData)));
    }

    @Test
    public void extractSessionDataPicksRemoteUser() throws Exception {
        MultivaluedMap<String,String> headers = new MultivaluedHashMap<>();
        headers.putSingle("AJP_hetu", "TEST_HETU");
        headers.putSingle("REMOTE_USER", "TEST_REMOTE_USER");
        ServiceProvider sp = new ServiceProvider(null, null);
        Map<String,String> sessionData = sp.extractSessionData(headers);
        assertThat(sessionData.get("AJP_hetu"), is("TEST_HETU"));
        assertThat(sessionData.get("REMOTE_USER"), is("TEST_REMOTE_USER"));
    }

    @Test
    public void extractSessionDataPicksAJPHeaders() throws Exception {
        MultivaluedMap<String,String> headers = new MultivaluedHashMap<>();
        headers.putSingle("AJP_uid", "TEST_UID");
        headers.putSingle("AJP_hetu", "TEST_HETU");
        headers.putSingle("AJP_satu", "TEST_SATU");
        headers.putSingle("AJP_eppn", "TEST_EPPN");
        headers.putSingle("AJP_cn", "TEST_COMMON_NAME");
        headers.putSingle("AJP_displayName", "TEST_DISPLAY_NAME");
        headers.putSingle("AJP_givenName", "TEST_GIVEN_NAME");
        headers.putSingle("AJP_sn", "TEST_SURNAME");
        headers.putSingle("AJP_authenticationProvider", "TEST_AUTH_PROVIDER");
        headers.putSingle("AJP_tfiKid", "TEST_KID");
        headers.putSingle("AJP_tfiPersonName", "TEST_TFI_PERSON_NAME");
        headers.putSingle("AJP_tfiVersion", "TEST_TFI_VERSION");
        headers.putSingle("AJP_tfiIdRef", "TEST_TFI_ID_REF");
        headers.putSingle("AJP_tfiNameRef", "TEST_TFI_NAME_REF");
        headers.putSingle("AJP_tfiCustName", "TEST_TFI_CUST_NAME");
        headers.putSingle("AJP_tfiCustId", "TEST_TFI_CUST_ID");
        headers.putSingle("AJP_tfiIdType", "TEST_TFI_ID_TYPE");
        headers.putSingle("AJP_mobileNumber", "TEST_MOBILE_NUMBER");
        ServiceProvider sp = new ServiceProvider(null, null);
        Map<String,String> sessionData = sp.extractSessionData(headers);
        assertThat(sessionData.get("AJP_uid"), is("TEST_UID"));
        assertThat(sessionData.get("AJP_hetu"), is("TEST_HETU"));
        assertThat(sessionData.get("AJP_satu"), is("TEST_SATU"));
        assertThat(sessionData.get("AJP_eppn"), is("TEST_EPPN"));
        assertThat(sessionData.get("AJP_cn"), is("TEST_COMMON_NAME"));
        assertThat(sessionData.get("AJP_displayName"), is("TEST_DISPLAY_NAME"));
        assertThat(sessionData.get("AJP_givenName"), is("TEST_GIVEN_NAME"));
        assertThat(sessionData.get("AJP_sn"), is("TEST_SURNAME"));
        assertThat(sessionData.get("AJP_authenticationProvider"), is("TEST_AUTH_PROVIDER"));
        assertThat(sessionData.get("AJP_tfiKid"), is("TEST_KID"));
        assertThat(sessionData.get("AJP_tfiPersonName"), is("TEST_TFI_PERSON_NAME"));
        assertThat(sessionData.get("AJP_tfiVersion"), is("TEST_TFI_VERSION"));
        assertThat(sessionData.get("AJP_tfiIdRef"), is("TEST_TFI_ID_REF"));
        assertThat(sessionData.get("AJP_tfiNameRef"), is("TEST_TFI_NAME_REF"));
        assertThat(sessionData.get("AJP_tfiCustName"), is("TEST_TFI_CUST_NAME"));
        assertThat(sessionData.get("AJP_tfiCustId"), is("TEST_TFI_CUST_ID"));
        assertThat(sessionData.get("AJP_tfiIdType"), is("TEST_TFI_ID_TYPE"));
        assertThat(sessionData.get("AJP_mobileNumber"), is("TEST_MOBILE_NUMBER"));
    }

    @Test
    public void extractSessionDataDoesNotMapsNonAJPHeaders() throws Exception {
        MultivaluedMap<String,String> headers = new MultivaluedHashMap<>();
        headers.putSingle("AJP_satu", "TEST_SATU");
        headers.putSingle("ANY_satu", "FAKE_SATU");
        ServiceProvider sp = new ServiceProvider(null, null);
        Map<String,String> sessionData = sp.extractSessionData(headers);
        assertThat(sessionData.get("AJP_satu"), is("TEST_SATU"));
        assertThat(sessionData.get("ANY_satu"), is(nullValue()));
    }
}
