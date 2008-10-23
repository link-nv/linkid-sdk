/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2.request;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.sdk.auth.saml2.Challenge;
import net.link.safeonline.sdk.auth.saml2.DomUtils;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
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
 * Factory class for Device Operation requests.
 * 
 * <p>
 * We're using the OpenSAML2 Java library for construction of the XML SAML documents.
 * </p>
 * 
 * @author wvdhaute
 * 
 */
public class DeviceOperationRequestFactory {

    private DeviceOperationRequestFactory() {

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
            Configuration.registerObjectProvider(DeviceOperationRequest.DEFAULT_ELEMENT_NAME,
                    new DeviceOperationRequestBuilder(), new DeviceOperationRequestMarshaller(),
                    new DeviceOperationRequestUnmarshaller(), null);
        } catch (ConfigurationException e) {
            throw new RuntimeException("could not bootstrap the OpenSAML2 library");
        }
    }


    /**
     * Creates a SAML2 device operation request. This request will contain a Subject element, containing the OLAS user
     * ID on the destination device node. It will also contain the device with which the user has authenticated before
     * issuing this device operation request.
     * 
     * The request can also optionally contain the device user attribute to specify the specific device registration for
     * this device operation.
     * 
     * 
     * @param issuerName
     * @param subjectName
     *            the subject name which wants to execute a device operation ( register/removal/update/disable ). This
     *            is OLAS user ID on the destination device node.
     * @param signerKeyPair
     * @param serviceURL
     *            the location of the service that will handle the response message.
     * @param destinationURL
     *            the location of the destination IdP.
     * @param deviceOperation
     *            the requested device operation.
     * @param challenge
     *            the challenge (output variable).
     * @param device
     * @param attribute
     */
    public static String createDeviceOperationRequest(String issuerName, String subjectName, KeyPair signerKeyPair,
            String serviceURL, String destinationURL, DeviceOperationType deviceOperation, Challenge<String> challenge,
            String device, String authenticatedDevice, String attribute) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("application name should not be null");
        if (null == destinationURL)
            throw new IllegalArgumentException("destination url should not be null");
        if (null == deviceOperation)
            throw new IllegalArgumentException("device operation should not be null");
        if (null == serviceURL)
            throw new IllegalArgumentException("service url should not be null");
        if (null == device)
            throw new IllegalArgumentException("device should not be null");

        DeviceOperationRequest request = buildXMLObject(DeviceOperationRequest.class,
                DeviceOperationRequest.DEFAULT_ELEMENT_NAME);

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

        request.setServiceURL(serviceURL);
        request.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        request.setDestination(destinationURL);
        request.setDeviceOperation(deviceOperation.name());
        request.setDevice(device);
        request.setAuthenticatedDevice(authenticatedDevice);
        request.setAttribute(attribute);

        return signRequest(request, signerKeyPair);
    }

    /**
     * Signs the unsigned device operation request
     * 
     * @return
     */
    private static String signRequest(DeviceOperationRequest request, KeyPair signerKeyPair) {

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
        request.setSignature(signature);
        BasicCredential signingCredential = SecurityHelper.getSimpleCredential(signerKeyPair.getPublic(), signerKeyPair
                .getPrivate());
        signature.setSigningCredential(signingCredential);

        // marshalling
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(request);
        Element requestElement;
        try {
            requestElement = marshaller.marshall(request);
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
