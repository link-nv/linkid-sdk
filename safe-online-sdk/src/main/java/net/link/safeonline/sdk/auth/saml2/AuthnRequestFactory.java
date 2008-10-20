/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Subject;
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
 * Factory class for SAML2 authentication requests.
 * 
 * <p>
 * We're using the OpenSAML2 Java library for construction of the XML SAML documents.
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class AuthnRequestFactory {

    private AuthnRequestFactory() {

        // empty
    }


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


    /**
     * Creates a SAML2 authentication request. For the moment we allow the Service Provider to pass on the Assertion
     * Consumer Service URL itself. Later on we could use the SAML Metadata service or a persistent server-side
     * application field to locate this service.
     * 
     * @param issuerName
     * @param applicationName
     * @param applicationFriendlyName
     * @param signerKeyPair
     * @param assertionConsumerServiceURL
     *            the optional location of the assertion consumer service. This location can be used by the IdP to send
     *            back the SAML response message.
     * @param destinationURL
     *            the optional location of the destination IdP.
     * @param challenge
     *            the optional challenge (output variable).
     * @param devices
     *            the optional list of allowed authentication devices.
     */
    public static String createAuthnRequest(String issuerName, String applicationName, String applicationFriendlyName,
            KeyPair signerKeyPair, String assertionConsumerServiceURL, String destinationURL,
            Challenge<String> challenge, Set<String> devices, boolean ssoEnabled) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == applicationName)
            throw new IllegalArgumentException("application name should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("application name should not be null");

        AuthnRequest request = buildXMLObject(AuthnRequest.class, AuthnRequest.DEFAULT_ELEMENT_NAME);

        request.setForceAuthn(!ssoEnabled);
        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        String id = idGenerator.generateIdentifier();
        request.setID(id);
        if (null != challenge) {
            challenge.setValue(id);
        }
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(new DateTime());
        Issuer issuer = buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(issuerName);
        request.setIssuer(issuer);

        if (null != assertionConsumerServiceURL) {
            request.setAssertionConsumerServiceURL(assertionConsumerServiceURL);
            request.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        }

        if (null != destinationURL) {
            request.setDestination(destinationURL);
        }

        if (null == applicationFriendlyName) {
            request.setProviderName(applicationName);
        } else {
            request.setProviderName(applicationFriendlyName);
        }

        NameIDPolicy nameIdPolicy = buildXMLObject(NameIDPolicy.class, NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIdPolicy.setAllowCreate(true);
        request.setNameIDPolicy(nameIdPolicy);

        if (null != devices) {
            RequestedAuthnContext requestedAuthnContext = buildXMLObject(RequestedAuthnContext.class,
                    RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
            List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
            for (String device : devices) {
                AuthnContextClassRef authnContextClassRef = buildXMLObject(AuthnContextClassRef.class,
                        AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
                authnContextClassRef.setAuthnContextClassRef(device);
                authnContextClassRefs.add(authnContextClassRef);
            }
            request.setRequestedAuthnContext(requestedAuthnContext);
        }

        Conditions conditions = buildXMLObject(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        AudienceRestriction audienceRestriction = buildXMLObject(AudienceRestriction.class,
                AudienceRestriction.DEFAULT_ELEMENT_NAME);
        audienceRestrictions.add(audienceRestriction);
        List<Audience> audiences = audienceRestriction.getAudiences();
        Audience audience = buildXMLObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
        audiences.add(audience);
        audience.setAudienceURI(applicationName);
        request.setConditions(conditions);

        return signAuthnRequest(request, signerKeyPair);
    }

    /**
     * Creates a SAML2 device operation authentication request. This authentication request will contain a Subject
     * element, containing the device mapping id.
     * 
     * @param issuerName
     * @param subjectName
     *            the subject name which wants to execute a device operation ( register/removal/update ). This is the
     *            device mapping id.
     * @param signerKeyPair
     * @param assertionConsumerServiceURL
     *            the optional location of the assertion consumer service. This location can be used by the IdP to send
     *            back the SAML response message.
     * @param destinationURL
     *            the optional location of the destination IdP.
     * @param challenge
     *            the optional challenge (output variable).
     * @param device
     */
    public static String createDeviceOperationAuthnRequest(String issuerName, String subjectName,
            KeyPair signerKeyPair, String assertionConsumerServiceURL, String destinationURL,
            DeviceOperationType deviceOperation, Challenge<String> challenge, String device) {
        
        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("application name should not be null");
        if (null == destinationURL)
            throw new IllegalArgumentException("destination url should not be null");
        if (null == deviceOperation)
            throw new IllegalArgumentException("device operation should not be null");

        AuthnRequest request = buildXMLObject(AuthnRequest.class, AuthnRequest.DEFAULT_ELEMENT_NAME);

        request.setForceAuthn(true);
        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        String id = idGenerator.generateIdentifier();
        request.setID(id);
        if (null != challenge) {
            challenge.setValue(id);
        }
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(new DateTime());
        Issuer issuer = buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(issuerName);
        request.setIssuer(issuer);

        Subject subject = buildXMLObject(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = buildXMLObject(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(subjectName);
        nameID.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        subject.setNameID(nameID);
        request.setSubject(subject);

        if (null != assertionConsumerServiceURL) {
            request.setAssertionConsumerServiceURL(assertionConsumerServiceURL);
            request.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        }

        request.setDestination(destinationURL);

        /*
         * Add device operation as an audience restriction to the conditions of the authnentication request.
         */
        Conditions conditions = buildXMLObject(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        AudienceRestriction audienceRestriction = buildXMLObject(AudienceRestriction.class,
                AudienceRestriction.DEFAULT_ELEMENT_NAME);
        audienceRestrictions.add(audienceRestriction);
        List<Audience> audiences = audienceRestriction.getAudiences();
        Audience audience = buildXMLObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
        audiences.add(audience);
        audience.setAudienceURI(deviceOperation.name());
        request.setConditions(conditions);

        NameIDPolicy nameIdPolicy = buildXMLObject(NameIDPolicy.class, NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIdPolicy.setAllowCreate(true);
        request.setNameIDPolicy(nameIdPolicy);

        RequestedAuthnContext requestedAuthnContext = buildXMLObject(RequestedAuthnContext.class,
                RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();

        AuthnContextClassRef authnContextClassRef = buildXMLObject(AuthnContextClassRef.class,
                AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(device);
        authnContextClassRefs.add(authnContextClassRef);

        request.setRequestedAuthnContext(requestedAuthnContext);

        return signAuthnRequest(request, signerKeyPair);
    }

    /**
     * Signs the unsigned authentication request
     * 
     * @return
     */
    private static String signAuthnRequest(AuthnRequest authnRequest, KeyPair signerKeyPair) {

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
        authnRequest.setSignature(signature);
        BasicCredential signingCredential = SecurityHelper.getSimpleCredential(signerKeyPair.getPublic(), signerKeyPair
                .getPrivate());
        signature.setSigningCredential(signingCredential);

        // marshalling
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(authnRequest);
        Element requestElement;
        try {
            requestElement = marshaller.marshall(authnRequest);
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
    private static <Type extends SAMLObject> Type buildXMLObject(@SuppressWarnings("unused") Class<Type> clazz,
            QName objectQName) {

        XMLObjectBuilder<Type> builder = Configuration.getBuilderFactory().getBuilder(objectQName);
        if (builder == null)
            throw new RuntimeException("Unable to retrieve builder for object QName " + objectQName);
        Type object = builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName
                .getPrefix());
        return object;
    }
}
