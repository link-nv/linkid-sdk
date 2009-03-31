/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.auth.saml2.response;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import net.link.safeonline.saml.common.Saml2SubjectConfirmationMethod;
import net.link.safeonline.saml.common.Saml2Util;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.ConfigurationException;


/**
 * Factory for SAML2 authentication responses.
 * 
 * @author fcorneli
 * 
 */
public class AuthnResponseFactory {

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


    private AuthnResponseFactory() {

        // empty
    }

    /**
     * Creates a signed authentication response.
     * 
     * @param audienceName
     *            This can be or the application name authenticated for, or the device operation executed
     */
    public static String createAuthResponse(String inResponseTo, String audienceName, String issuerName, String subjectName,
                                            String samlName, KeyPair signerKeyPair, int validity, String target) {

        Map<DateTime, String> authentications = new HashMap<DateTime, String>();
        authentications.put(new DateTime(), samlName);
        return createAuthResponse(inResponseTo, audienceName, issuerName, subjectName, signerKeyPair, validity, target, authentications);
    }

    public static String createAuthResponse(String inResponseTo, String audienceName, String issuerName, String subjectName,
                                            KeyPair signerKeyPair, int validity, String target, Map<DateTime, String> authentications) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("issuer name should not be null");
        if (null == subjectName)
            throw new IllegalArgumentException("subject name should not be null");
        if (null == audienceName)
            throw new IllegalArgumentException("audience name should not be null");

        Response response = Saml2Util.buildXMLObject(Response.class, Response.DEFAULT_ELEMENT_NAME);

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
        statusCode.setValue(StatusCode.SUCCESS_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        Assertion assertion = Saml2Util.getAssertion(inResponseTo, audienceName, subjectName, issuerName, validity, target,
                authentications, Saml2SubjectConfirmationMethod.BEARER, null);
        response.getAssertions().add(assertion);

        return Saml2Util.sign(response, signerKeyPair);
    }

    /**
     * Creates a signed authentication response with status failed.
     */
    public static String createAuthResponseFailed(String inResponseTo, String issuerName, KeyPair signerKeyPair, String target) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("issuer name should not be null");

        Response response = Saml2Util.buildXMLObject(Response.class, Response.DEFAULT_ELEMENT_NAME);

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
        statusCode.setValue(StatusCode.AUTHN_FAILED_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        return Saml2Util.sign(response, signerKeyPair);
    }

    /**
     * Creates a signed authentication response with status unknown principal, indicating user requests a registration.
     */
    public static String createAuthResponseRequestRegistration(String inResponseTo, String issuerName, KeyPair signerKeyPair, String target) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("issuer name should not be null");

        Response response = Saml2Util.buildXMLObject(Response.class, Response.DEFAULT_ELEMENT_NAME);

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
        statusCode.setValue(StatusCode.UNKNOWN_PRINCIPAL_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);

        return Saml2Util.sign(response, signerKeyPair);
    }
}
