/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.saml.common;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthenticatorBaseType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthenticatorTransportProtocolType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthnContextDeclarationBaseType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.AuthnMethodBaseType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.ExtensionOnlyType;
import oasis.names.tc.saml._2_0.ac.classes.passwordprotectedtransport.ObjectFactory;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.KeyInfoConfirmationDataType;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * <h2>{@link Saml2Util}<br>
 * <sub>Utility class for SAML.</sub></h2>
 * 
 * <p>
 * Utility class for SAML.
 * </p>
 * 
 * <p>
 * <i>Dec 16, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class Saml2Util {

    @SuppressWarnings("unchecked")
    public static <Type extends XMLObject> Type buildXMLObject(@SuppressWarnings("unused") Class<Type> clazz, QName objectQName) {

        XMLObjectBuilder<Type> builder = Configuration.getBuilderFactory().getBuilder(objectQName);
        if (builder == null)
            throw new RuntimeException("Unable to retrieve builder for object QName " + objectQName);
        Type object = builder.buildObject(objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix());
        return object;
    }

    /**
     * Marshalls the given SAMLObject.
     * 
     * @return
     */
    public static String marshall(SAMLObject samlObject) {

        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(samlObject);
        Element requestElement;
        try {
            requestElement = marshaller.marshall(samlObject);
        } catch (MarshallingException e) {
            throw new RuntimeException("opensaml2 marshalling error: " + e.getMessage(), e);
        }

        String result;
        try {
            result = DomUtils.domToString(requestElement);
        } catch (TransformerException e) {
            throw new RuntimeException("DOM to string error: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * Signs and marshalls the given SignableSAMLObject.
     */
    public static String sign(SignableSAMLObject samlObject, KeyPair signerKeyPair) {

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory.getBuilder(Signature.DEFAULT_ELEMENT_NAME);
        Signature signature = signatureBuilder.buildObject();
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        String algorithm = signerKeyPair.getPrivate().getAlgorithm();
        if ("RSA".equals(algorithm)) {
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA);
        } else if ("DSA".equals(algorithm)) {
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_DSA);
        }
        samlObject.setSignature(signature);
        BasicCredential signingCredential = SecurityHelper.getSimpleCredential(signerKeyPair.getPublic(), signerKeyPair.getPrivate());
        signature.setSigningCredential(signingCredential);

        // marshalling
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(samlObject);
        Element requestElement;
        try {
            requestElement = marshaller.marshall(samlObject);
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

    public static Assertion getAssertion(String inResponseTo, String audienceName, String subjectName, String issuerName, String samlName,
                                         int validity, String target, DateTime authenticationDate,
                                         Saml2SubjectConfirmationMethod subjectConfirmationMethod, PublicKey publicKey) {

        DateTime now = new DateTime();
        DateTime notAfter = now.plusSeconds(validity);

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }

        Assertion assertion = Saml2Util.buildXMLObject(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID(idGenerator.generateIdentifier());
        assertion.setIssueInstant(now);

        Issuer assertionIssuer = Saml2Util.buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        assertionIssuer.setValue(issuerName);
        assertion.setIssuer(assertionIssuer);

        Subject subject = Saml2Util.buildXMLObject(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = Saml2Util.buildXMLObject(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(subjectName);
        nameID.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        subject.setNameID(nameID);
        assertion.setSubject(subject);

        Conditions conditions = Saml2Util.buildXMLObject(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(notAfter);

        if (null != audienceName) {
            List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
            AudienceRestriction audienceRestriction = buildXMLObject(AudienceRestriction.class, AudienceRestriction.DEFAULT_ELEMENT_NAME);
            audienceRestrictions.add(audienceRestriction);
            List<Audience> audiences = audienceRestriction.getAudiences();
            Audience audience = buildXMLObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
            audiences.add(audience);
            audience.setAudienceURI(audienceName);
        }

        assertion.setConditions(conditions);

        List<SubjectConfirmation> subjectConfirmations = subject.getSubjectConfirmations();
        SubjectConfirmation subjectConfirmation = Saml2Util.buildXMLObject(SubjectConfirmation.class,
                SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        subjectConfirmation.setMethod(subjectConfirmationMethod.getMethodURI());

        SubjectConfirmationData subjectConfirmationData;
        if (subjectConfirmationMethod.equals(Saml2SubjectConfirmationMethod.HOLDER_OF_KEY) && null != publicKey) {
            subjectConfirmationData = Saml2Util.buildXMLObject(SubjectConfirmationData.class, KeyInfoConfirmationDataType.TYPE_NAME);
            KeyInfo keyInfo = Saml2Util.buildXMLObject(KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME);
            KeyInfoHelper.addPublicKey(keyInfo, publicKey);
            subjectConfirmationData.getUnknownXMLObjects().add(keyInfo);
        } else {
            subjectConfirmationData = Saml2Util.buildXMLObject(SubjectConfirmationData.class, SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
        }
        if (null != target) {
            subjectConfirmationData.setRecipient(target);
        }
        subjectConfirmationData.setInResponseTo(inResponseTo);
        subjectConfirmationData.setNotBefore(now);
        subjectConfirmationData.setNotOnOrAfter(notAfter);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subjectConfirmations.add(subjectConfirmation);

        AuthnStatement authnStatement = Saml2Util.buildXMLObject(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        assertion.getAuthnStatements().add(authnStatement);
        authnStatement.setAuthnInstant(authenticationDate);
        AuthnContext authnContext = Saml2Util.buildXMLObject(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnContext(authnContext);

        AuthnContextClassRef authnContextClassRef = Saml2Util.buildXMLObject(AuthnContextClassRef.class,
                AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnContextClassRef.setAuthnContextClassRef(samlName);

        return assertion;
    }

    @SuppressWarnings("unused")
    private static Document createPasswordDeclaration() {

        ObjectFactory objectFactory = new ObjectFactory();
        AuthnContextDeclarationBaseType authnContextDeclaration = objectFactory.createAuthnContextDeclarationBaseType();
        AuthnMethodBaseType authnMethod = objectFactory.createAuthnMethodBaseType();
        AuthenticatorBaseType authenticator = objectFactory.createAuthenticatorBaseType();
        authnMethod.setAuthenticator(authenticator);
        AuthenticatorTransportProtocolType authenticatorTransportProtocol = objectFactory.createAuthenticatorTransportProtocolType();
        ExtensionOnlyType ssl = objectFactory.createExtensionOnlyType();
        authenticatorTransportProtocol.setSSL(ssl);
        authnMethod.setAuthenticatorTransportProtocol(authenticatorTransportProtocol);
        authnContextDeclaration.setAuthnMethod(authnMethod);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("DOM error");
        }
        Document document = documentBuilder.newDocument();

        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            javax.xml.bind.Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(objectFactory.createAuthenticationContextDeclaration(authnContextDeclaration), document);
            return document;
        } catch (JAXBException e) {
            throw new RuntimeException("JAXB error: " + e.getMessage(), e);
        }
    }

}
