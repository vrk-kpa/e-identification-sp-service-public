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

import fi.vm.kapa.identification.client.FeedbackClient;
import fi.vm.kapa.identification.exception.InvalidRequestException;
import fi.vm.kapa.identification.model.EidasContactForm;
import fi.vm.kapa.identification.model.EidasSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Component
@Path("/eidas-email")
public class EidasEmailRedirectResource {

    private static final Logger logger = LoggerFactory.getLogger(EidasEmailRedirectResource.class);


    private final FeedbackClient feedbackClient;

    @Autowired
    public EidasEmailRedirectResource(FeedbackClient feedbackClient) {
        this.feedbackClient = feedbackClient;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
    public Response getEidasEmailRedirect(@Context HttpServletRequest request,
                                          EidasContactForm contactFormData) {

        String logtag = "";
        try {

            HttpSession session = request.getSession(false);
            if ( session != null && session.getAttribute(EidasSession.SESSION_ATTRIBUTE) != null ) {
                EidasSession eidasSession = (EidasSession) session.getAttribute(EidasSession.SESSION_ATTRIBUTE);
                logtag = eidasSession.getLogTag();

                if ( !eidasSession.getEntityID().equals(contactFormData.getEntityID()) ) {
                    logger.warn("<<{}>> Session serviceProvider entityID {} does not match request entityID {}", logtag, eidasSession.getEntityID(), contactFormData.getEntityID());
                    throw new InvalidRequestException();
                }
                // enrich contact form data with serviceprovider specific data
                contactFormData.setServiceName(eidasSession.getDisplayNameFI());
                contactFormData.setEidasContactAddress(eidasSession.getEidasContactAddress());
                contactFormData.setTag(logtag);
                feedbackClient.postData(contactFormData);

                session.invalidate();
            }
            else {
                throw new InvalidRequestException();
            }

            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            logger.warn("<<{}>> Failed to send eIDAS contact email: {}", logtag, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}

