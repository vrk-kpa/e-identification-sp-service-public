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

import fi.vm.kapa.identification.dto.ProxyMessageDTO;
import fi.vm.kapa.identification.model.EidasFlow;

import java.util.Map;
import java.util.StringJoiner;

/*
Builder class for one audit log row
Null values are logged as empty strings
*/
public class AuditLogRowBuilder {

    private String sessionIndex = "";
    private String authnContextDecl = "";
    private String authnContextClass = "";
    private String authnInstant = "";
    private String identityProviderName = "";
    private String userName = "";
    private String levelOfAssurance = "";
    private String logTag = "";
    private String hetu = "";
    private String satu = "";
    private String kid = "";
    private String eidasPID = "";
    private String eidasFlow = "";

    public AuditLogRowBuilder(Map<String,String> sessionData, ProxyMessageDTO message, String logTag, EidasFlow eidasFlow) {
        setSessionIndex(sessionData.get("AJP_Shib-Session-Index"));
        setAuthnContextDecl(sessionData.get("AJP_Shib-AuthnContext-Decl"));
        setAuthnContextClass(sessionData.get("AJP_Shib-AuthnContext-Class"));
        setAuthnInstant(sessionData.get("AJP_Shib-Authentication-Instant"));
        setIdentityProviderName(sessionData.get("AJP_Shib-Identity-Provider"));
        setUserName(message.getUid());
        setLevelOfAssurance(message.getLevelOfAssurance());
        setLogTag(logTag);
        setHetu(sessionData.get("AJP_hetu"));
        setSatu(sessionData.get("AJP_satu"));
        setKid(sessionData.get("AJP_tfiKid"));
        setEidasPID(sessionData.get("AJP_eidasPersonIdentifier"));
        setEidasFlow(eidasFlow);
    }

    public void setSessionIndex(String sessionIndex) {
        this.sessionIndex = sessionIndex == null ? "" : sessionIndex;
    }

    public void setAuthnContextDecl(String authnContextDecl) {
        this.authnContextDecl = authnContextDecl == null ? "" : authnContextDecl;
    }

    public void setAuthnContextClass(String authnContextClass) {
        this.authnContextClass = authnContextClass == null ? "" : authnContextClass;
    }

    public void setAuthnInstant(String authnInstant) {
        this.authnInstant = authnInstant == null ? "" : authnInstant;
    }

    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName == null ? "" : identityProviderName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? "" : userName;
    }

    public void setLevelOfAssurance(String levelOfAssurance) {
        this.levelOfAssurance = levelOfAssurance == null ? "" : levelOfAssurance;
    }

    public void setLogTag(String logTag) {
        this.logTag = logTag == null ? "" : logTag;
    }

    public void setHetu(String hetu) {
        this.hetu = hetu == null ? "" : hetu;
    }

    public void setSatu(String satu) {
        this.satu = satu == null ? "" : satu;
    }

    public void setKid(String kid) {
        this.kid = kid == null ? "" : kid;
    }

    public void setEidasPID(String eidasPID) {
        this.eidasPID = eidasPID == null ? "" : eidasPID;
    }

    public void setEidasFlow(EidasFlow eidasFlow) {
        this.eidasFlow = eidasFlow.getText() == null ? "" : eidasFlow.getText(); }

    public String build() {
        StringJoiner row = new StringJoiner("|", "", "|");
        row.add(sessionIndex)
            .add(authnContextDecl)
            .add(authnContextClass)
            .add(authnInstant)
            .add(identityProviderName)
            .add(userName)
            .add(levelOfAssurance)
            .add(logTag)
            .add(hetu)
            .add(satu)
            .add(kid)
            .add(eidasPID)
            .add(eidasFlow);

        return row.toString();
    }

}
