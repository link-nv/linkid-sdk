/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import net.link.safeonline.saml.common.Saml2Util;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.ConfigurationException;


/**
 * Factory for SAML2 logout responses.
 * 
 * @author wvdhaute
 * 
 */
public class LogoutResponseFactory {

    static {
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException("could not bootstrap the OpenSAML2 library");
        }
    }


    private LogoutResponseFactory() {

        // empty
    }

    public static String createLogoutResponse(String inResponseTo, String issuerName, KeyPair signerKeyPair, String target) {

        return createLogoutResponse(false, inResponseTo, issuerName, signerKeyPair, target);
    }

    public static String createLogoutResponse(boolean partialLogout, String inResponseTo, String issuerName, KeyPair signerKeyPair,
                                              String target) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("issuer name should not be null");

        LogoutResponse response = Saml2Util.buildXMLObject(LogoutResponse.class, LogoutResponse.DEFAULT_ELEMENT_NAME);

        DateTime now = new DateTime();

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        response.setID(idGenerator.generateIdentifier());
        response.setVersion(SAMLVersion.VERSION_20);
        response.setInResponseTo(inResponseTo);
        response.setIssueInstant(now);

        Issuer responseIssuer = Saml2Util.buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        responseIssuer.setValue(issuerName);
        response.setIssuer(responseIssuer);

        response.setDestination(target);

        Status status = Saml2Util.buildXMLObject(Status.class, Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = Saml2Util.buildXMLObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        if (partialLogout) {
            statusCode.setValue(StatusCode.PARTIAL_LOGOUT_URI);
        } else {
            statusCode.setValue(StatusCode.SUCCESS_URI);
        }
        status.setStatusCode(statusCode);
        response.setStatus(status);

        return Saml2Util.sign(response, signerKeyPair);
    }

}
