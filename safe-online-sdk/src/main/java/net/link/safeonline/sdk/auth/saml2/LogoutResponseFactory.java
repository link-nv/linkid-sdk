/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.w3c.dom.Element;


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

    public static String createLogoutResponse(String inResponseTo, String issuerName, KeyPair signerKeyPair,
            String target) {

        return createLogoutResponse(false, inResponseTo, issuerName, signerKeyPair, target);
    }

    public static String createLogoutResponse(boolean partialLogout, String inResponseTo, String issuerName,
            KeyPair signerKeyPair, String target) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("issuer name should not be null");

        LogoutResponse response = buildXMLObject(LogoutResponse.class, LogoutResponse.DEFAULT_ELEMENT_NAME);

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

        Issuer responseIssuer = buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        responseIssuer.setValue(issuerName);
        response.setIssuer(responseIssuer);

        response.setDestination(target);

        Status status = buildXMLObject(Status.class, Status.DEFAULT_ELEMENT_NAME);
        StatusCode statusCode = buildXMLObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        if (partialLogout) {
            statusCode.setValue(StatusCode.PARTIAL_LOGOUT_URI);
        } else {
            statusCode.setValue(StatusCode.SUCCESS_URI);
        }
        status.setStatusCode(statusCode);
        response.setStatus(status);

        return signLogoutResponse(response, signerKeyPair);
    }

    /**
     * Sign the unsigned authentication response.
     */
    private static String signLogoutResponse(LogoutResponse response, KeyPair signerKeyPair) {

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory
                .getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        Signature signature = signatureBuilder.buildObject();
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        String algorithm = signerKeyPair.getPrivate().getAlgorithm();
        if ("RSA".equals(algorithm)) {
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);
        } else if ("DSA".equals(algorithm)) {
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_DSA);
        }
        response.setSignature(signature);
        BasicCredential signingCredential = SecurityHelper.getSimpleCredential(signerKeyPair.getPublic(), signerKeyPair
                .getPrivate());
        signature.setSigningCredential(signingCredential);

        // marshalling
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(response);
        Element requestElement;
        try {
            requestElement = marshaller.marshall(response);
        } catch (MarshallingException e) {
            throw new RuntimeException("opensaml2 marshalling error: " + e.getMessage(), e);
        }

        // sign after marshaling of course
        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            throw new RuntimeException("opensaml2 signing error: " + e.getMessage(), e);
        }

        String result;
        try {
            result = DomUtils.domToString(requestElement);
        } catch (TransformerException e) {
            throw new RuntimeException("DOM to string error: " + e.getMessage(), e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <Type extends SAMLObject> Type buildXMLObject(@SuppressWarnings("unused") Class<Type> clazz,
            QName objectQName) {

        XMLObjectBuilder<Type> builder = Configuration.getBuilderFactory().getBuilder(objectQName);
        if (builder == null)
            throw new RuntimeException("Unable to retrieve builder for object QName " + objectQName);
        Type object = builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName
                .getPrefix());
        return object;
    }
}
