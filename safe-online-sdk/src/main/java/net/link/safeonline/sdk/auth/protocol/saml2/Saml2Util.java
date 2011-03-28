/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.protocol.saml2;

import be.fedict.trust.MemoryCertificateRepository;
import be.fedict.trust.TrustValidator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.attribute.provider.Compound;
import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
import net.link.safeonline.sdk.ws.WebServiceConstants;
import net.link.util.common.DomUtils;
import net.link.util.common.KeyStoreUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.binding.security.SAML2HTTPRedirectDeflateSignatureRule;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.*;
import org.opensaml.xml.io.*;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.StaticCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Element;


/**
 * <h2>{@link Saml2Util}<br> <sub>Utility class for SAML.</sub></h2>
 *
 * <p> Utility class for SAML. </p>
 *
 * <p> <i>Dec 16, 2008</i> </p>
 *
 * @author wvdhaute
 */
public abstract class Saml2Util {

    private static final Log LOG = LogFactory.getLog( Saml2Util.class );

    static {
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                            "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException( "could not bootstrap the OpenSAML2 library", e );
        }
    }

    public static <T extends XMLObject> T buildXMLObject(@SuppressWarnings("unused") Class<T> clazz, QName objectQName) {

        @SuppressWarnings("unchecked")
        XMLObjectBuilder<T> builder = Configuration.getBuilderFactory().getBuilder( objectQName );
        if (builder == null)
            throw new RuntimeException( "Unable to retrieve builder for object QName " + objectQName );

        return builder.buildObject( objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix() );
    }

    public static Element marshall(SAMLObject samlObject) {

        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller( samlObject );

        try {
            return marshaller.marshall( samlObject );
        } catch (MarshallingException e) {
            throw new RuntimeException( "opensaml2 marshalling error: " + e.getMessage(), e );
        }
    }

    public static XMLObject unmarshall(Element samlElement) {

        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller( samlElement );

        try {
            XMLObject xmlObject = unmarshaller.unmarshall( samlElement );
            xmlObject.addNamespace(
                    new Namespace( WebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE, WebServiceConstants.SAFE_ONLINE_SAML_PREFIX ) );
            return xmlObject;
        } catch (UnmarshallingException e) {
            throw new RuntimeException( "opensaml2 unmarshalling error: " + e.getMessage(), e );
        }
    }

    public static String deflateAndBase64Encode(SAMLObject message)
            throws MessageEncodingException, TransformerException, IOException {
        LOG.debug( "Deflating and Base64 encoding SAML message" );
        String messageStr = DomUtils.domToString( marshall( message ) );

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        Deflater deflater = new Deflater( Deflater.DEFLATED, true );
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream( bytesOut, deflater );
        try {
            deflaterStream.write( messageStr.getBytes( "UTF-8" ) );
            deflaterStream.finish();
        } finally {
            deflaterStream.close();
        }

        return Base64.encodeBytes( bytesOut.toByteArray(), Base64.DONT_BREAK_LINES );
    }

    public static Element signAsElement(SignableSAMLObject samlObject, KeyPair signerKeyPair, List<X509Certificate> certificateChain) {

        XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory.getBuilder( Signature.DEFAULT_ELEMENT_NAME );
        Signature signature = signatureBuilder.buildObject();
        signature.setCanonicalizationAlgorithm( SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS );

        String algorithm = signerKeyPair.getPrivate().getAlgorithm();
        if ("RSA".equals( algorithm ))
            signature.setSignatureAlgorithm( SignatureConstants.ALGO_ID_SIGNATURE_RSA );
        else if ("DSA".equals( algorithm ))
            signature.setSignatureAlgorithm( SignatureConstants.ALGO_ID_SIGNATURE_DSA );

        BasicX509Credential signingCredential = new BasicX509Credential();
        signingCredential.setPrivateKey( signerKeyPair.getPrivate() );

        if (null != certificateChain) {
            // enable adding the cert.chain as KeyInfo
            X509KeyInfoGeneratorFactory factory = (X509KeyInfoGeneratorFactory) Configuration.getGlobalSecurityConfiguration()
                    .getKeyInfoGeneratorManager()
                    .getDefaultManager()
                    .getFactory( signingCredential );
            factory.setEmitEntityCertificateChain( true );
            signingCredential.setEntityCertificateChain( certificateChain );

            // add certificate chain as keyinfo
            signature.setKeyInfo( getKeyInfo( certificateChain ) );
        } else {
            signingCredential.setPublicKey( signerKeyPair.getPublic() );

            // add public key as keyinfo
            signature.setKeyInfo( getKeyInfo( signerKeyPair.getPublic() ) );
        }

        signature.setSigningCredential( signingCredential );
        samlObject.setSignature( signature );

        // Marshall so it has an XML representation.
        Element samlElement = marshall( samlObject );

        // Sign after marshaling so we can add a signature to the XML representation.
        try {
            Signer.signObject( signature );
        } catch (SignatureException e) {
            throw new RuntimeException( "opensaml2 signing error: " + e.getMessage(), e );
        }
        return samlElement;
    }

    private static KeyInfo getKeyInfo(List<X509Certificate> certificateChain) {

        KeyInfo keyInfo = buildXMLObject( KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME );
        try {
            for (X509Certificate certificate : certificateChain) {
                KeyInfoHelper.addCertificate( keyInfo, certificate );
            }
        } catch (CertificateEncodingException e) {
            throw new RuntimeException( "opensaml2 certificate encoding error: " + e.getMessage(), e );
        }
        return keyInfo;
    }

    private static KeyInfo getKeyInfo(PublicKey publicKey) {

        KeyInfo keyInfo = buildXMLObject( KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME );
        KeyInfoHelper.addPublicKey( keyInfo, publicKey );
        return keyInfo;
    }

    /**
     * Signs the given {@link SignableSAMLObject}.
     *
     * @param samlObject       SAML Object to sign
     * @param signerKeyPair    keypair to sign with
     * @param certificateChain optional certificate chain for offline validation
     *
     * @return The signed {@link SignableSAMLObject}, marshaled and serialized.
     */
    public static String sign(SignableSAMLObject samlObject, KeyPair signerKeyPair, List<X509Certificate> certificateChain) {

        Element samlElement = signAsElement( samlObject, signerKeyPair, certificateChain );

        // Dump our XML element to a string.
        return DomUtils.domToString( samlElement );
    }

    /**
     * Validate the specified opensaml XML Signature
     *
     * @param signature the XML signature
     *
     * @throws CertificateException something went wrong extracting the certificates from the XML Signature.
     * @throws ValidationException  validation failed
     * @throws KeyException         failed to extract public key data
     */
    public static void validateSignature(Signature signature)
            throws CertificateException, ValidationException, KeyException {

        List<X509Certificate> certChain = KeyInfoHelper.getCertificates( signature.getKeyInfo() );
        List<PublicKey> publicKeys = KeyInfoHelper.getPublicKeys( signature.getKeyInfo() );

        SAMLSignatureProfileValidator pv = new SAMLSignatureProfileValidator();
        pv.validate( signature );
        BasicX509Credential credential = new BasicX509Credential();

        if (!certChain.isEmpty())
            credential.setPublicKey( KeyStoreUtils.getEndCertificate( certChain ).getPublicKey() );
        else if (!publicKeys.isEmpty() && publicKeys.size() == 1)
            credential.setPublicKey( publicKeys.get( 0 ) );
        else
            throw new ValidationException( "Failed to validate XML Signature, no suitable KeyInfo found..." );

        SignatureValidator sigValidator = new SignatureValidator( credential );
        sigValidator.validate( signature );
    }

    /**
     * Validates the HTTP-Redirect binding signature
     *
     * @param queryString  query string
     * @param requestURL   request URL
     * @param certificates list of certificates to use for validation
     *
     * @return valid or not
     */
    public static boolean validateSignature(String queryString, StringBuffer requestURL, List<X509Certificate> certificates) {

        LOG.debug( "validate[HTTP Redirect], Query:\n" + queryString );

        List<Credential> credentials = new LinkedList<Credential>();
        for (X509Certificate certificate : certificates)
            credentials.add( SecurityHelper.getSimpleCredential( certificate, null ) );
        if (credentials.isEmpty()) {
            LOG.warn( "Signature not valid: There are no credentials to validate against." );
            return false;
        }

        StaticCredentialResolver credResolver = new StaticCredentialResolver( credentials );
        final SignatureTrustEngine engine = new ExplicitKeySignatureTrustEngine( credResolver,
                                                                                 Configuration.getGlobalSecurityConfiguration()
                                                                                         .getDefaultKeyInfoCredentialResolver() );

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport(
                new HttpServletRequestAdapter( new MockSAMLHttpServletRequest( queryString, requestURL ) ) );
        messageContext.setPeerEntityRole( SPSSODescriptor.DEFAULT_ELEMENT_NAME );
        messageContext.setSecurityPolicyResolver( new SecurityPolicyResolver() {

            public Iterable<SecurityPolicy> resolve(MessageContext criteria)
                    throws org.opensaml.xml.security.SecurityException {

                return Collections.singleton( resolveSingle( criteria ) );
            }

            public SecurityPolicy resolveSingle(MessageContext criteria)
                    throws org.opensaml.xml.security.SecurityException {

                SecurityPolicy securityPolicy = new BasicSecurityPolicy();
                securityPolicy.getPolicyRules().add( new SAML2HTTPRedirectDeflateSignatureRule( engine ) );

                return securityPolicy;
            }
        } );

        try {
            new HTTPRedirectDeflateDecoder().decode( messageContext );
        } catch (MessageDecodingException e) {
            LOG.warn( "Signature validation failed.", e );
            return false;
        } catch (SecurityException e) {
            LOG.warn( "Signature validation failed.", e );
            return false;
        }

        return true;
    }

    /**
     * Validates the signature. If {@link Signature} is <code>null</code> the HTTP-Redirect binding was used and the {@link X509Certificate}
     * serverCertificate is required for validation.
     *
     * @param signature           signature, if <code>null</code>, serviceCertificate is required
     * @param serviceCertificates optional serviceCertificates, required if signature is <code>null</code> and signature was set with
     *                            HTTP-Redirect binding.
     * @param request             HTTP Servlet Request
     *
     * @return optional embedded certificate chain in the signature
     *
     * @throws ValidationFailedException validation failed
     */
    public static List<X509Certificate> validateSignature(Signature signature, List<X509Certificate> serviceCertificates,
                                                          HttpServletRequest request)
            throws ValidationFailedException {

        List<X509Certificate> certificateChain = null;
        if (null == signature) {

            // HTTP-Redirect, ServerCertificate HAS to be specified ...
            if (null == serviceCertificates || serviceCertificates.isEmpty()) {
                throw new ValidationFailedException(
                        "Response returned in HTTP-Redirect binding, MUST specify LinkID certificate for validation of the signature!" );
            }

            boolean valid = validateSignature( request.getQueryString(), request.getRequestURL(), serviceCertificates );
            if (!valid) {
                throw new ValidationFailedException( "Invalid Signature on the SAML Response (using HTTP-Redirect)" );
            }
        } else {
            try {
                validateSignature( signature );
            } catch (CertificateException e) {
                throw new ValidationFailedException( e );
            } catch (ValidationException e) {
                throw new ValidationFailedException( e );
            } catch (KeyException e) {
                throw new ValidationFailedException( e );
            }

            // get certificate chain from signature
            try {
                certificateChain = KeyInfoHelper.getCertificates( signature.getKeyInfo() );
            } catch (CertificateException e) {
                throw new ValidationFailedException( e );
            }
        }

        return certificateChain;
    }

    public static void validateCertificateChain(List<X509Certificate> rootCertificates, List<X509Certificate> certificateChain)
            throws ValidationFailedException {

        MemoryCertificateRepository certificateRepository = new MemoryCertificateRepository();
        for (X509Certificate rootCertificate : rootCertificates) {
            certificateRepository.addTrustPoint( rootCertificate );
        }

        TrustValidator trustValidator = new TrustValidator( certificateRepository );
        try {
            trustValidator.isTrusted( certificateChain );
        } catch (CertPathValidatorException e) {
            LOG.error( "Trust validation of LinkID certificate chain failed!", e );
            throw new ValidationFailedException( e );
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, List<AttributeSDK<?>>> getAttributeValues(Assertion assertion) {

        Map<String, List<AttributeSDK<?>>> attributeMap = new HashMap<String, List<AttributeSDK<?>>>();
        List<AttributeStatement> attrStatements = assertion.getAttributeStatements();
        if (attrStatements == null || attrStatements.isEmpty())
            return attributeMap;

        AttributeStatement attributeStatement = attrStatements.get( 0 );

        for (Attribute attribute : attributeStatement.getAttributes()) {

            AttributeSDK<?> attributeSDK = getAttributeSDK( attribute );

            List<AttributeSDK<?>> attributes = attributeMap.get( attributeSDK.getName() );
            if (null == attributes) {
                attributes = new LinkedList<AttributeSDK<?>>();
            }
            attributes.add( attributeSDK );
            attributeMap.put( attributeSDK.getName(), attributes );
        }
        return attributeMap;
    }

    private static AttributeSDK<?> getAttributeSDK(Attribute attributeType) {

        String attributeId = attributeType.getUnknownAttributes().get( WebServiceConstants.ATTRIBUTE_ID );
        AttributeSDK<Serializable> attribute = new AttributeSDK<Serializable>( attributeId, attributeType.getName() );

        List<XMLObject> attributeValues = attributeType.getAttributeValues();
        if (attributeValues.isEmpty())
            return attribute;

        XMLObject xmlValue = attributeValues.get( 0 );
        if (null != xmlValue.getOrderedChildren() && !xmlValue.getOrderedChildren().isEmpty()) {

            // compound
            List<AttributeSDK<?>> compoundMembers = new LinkedList<AttributeSDK<?>>();
            for (XMLObject memberAttributeObject : attributeValues.get( 0 ).getOrderedChildren()) {

                Attribute memberAttribute = (Attribute) memberAttributeObject;
                AttributeSDK<Serializable> member = new AttributeSDK<Serializable>( attributeId, memberAttribute.getName() );
                if (!memberAttribute.getAttributeValues().isEmpty()) {
                    member.setValue( toJavaObject( memberAttribute.getAttributeValues().get( 0 ) ) );
                }
                compoundMembers.add( member );
            }
            attribute.setValue( new Compound( compoundMembers ) );
        } else {
            // single/multi valued
            attribute.setValue( toJavaObject( xmlValue ) );
        }
        return attribute;
    }

    public static XSAny getXmlObject(QName qName) {

        XSAnyBuilder anyBuilder = (XSAnyBuilder) Configuration.getBuilderFactory().getBuilder( XSAny.TYPE_NAME );
        return anyBuilder.buildObject( qName, XSAny.TYPE_NAME );
    }

    public static XMLObject toXmlObject(Object attributeValue) {

        LOG.debug( "converting value " + attributeValue + " to XML" );

        XSAnyBuilder anyBuilder = (XSAnyBuilder) Configuration.getBuilderFactory().getBuilder( XSAny.TYPE_NAME );
        XSAny anyValue = anyBuilder.buildObject( AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME );
        if (attributeValue == null) {
            anyValue.getUnknownAttributes().put( WebServiceConstants.XML_SCHEMA_INSTANCE_NIL, "true" );
            LOG.debug( "converting value " + attributeValue + " to XML: nil" );
        } else {
            String xsType, xsValue = String.valueOf( attributeValue );

            if (Boolean.class.isAssignableFrom( attributeValue.getClass() ))
                xsType = "xs:boolean";
            else if (Integer.class.isAssignableFrom( attributeValue.getClass() ))
                xsType = "xs:integer";
            else if (Long.class.isAssignableFrom( attributeValue.getClass() ))
                xsType = "xs:long";
            else if (Short.class.isAssignableFrom( attributeValue.getClass() ))
                xsType = "xs:short";
            else if (Byte.class.isAssignableFrom( attributeValue.getClass() ))
                xsType = "xs:byte";
            else if (Float.class.isAssignableFrom( attributeValue.getClass() ))
                xsType = "xs:float";
            else if (Double.class.isAssignableFrom( attributeValue.getClass() ))
                xsType = "xs:double";
            else if (Date.class.isAssignableFrom( attributeValue.getClass() )) {
                xsType = "xs:dateTime";
                xsValue = new DateTime( ((Date) attributeValue).getTime() ).toString();
            } else
                xsType = "xs:string";

            anyValue.getUnknownAttributes().put( WebServiceConstants.XML_SCHEMA_INSTANCE_TYPE, xsType );
            anyValue.setTextContent( xsValue );
            LOG.debug( "converting value " + attributeValue + " of type " + attributeValue.getClass() + " to XML: xsType = " + xsType
                       + ", xsValue = " + xsValue );
        }

        return anyValue;
    }

    public static Serializable toJavaObject(XMLObject attributeValue) {

        if (Boolean.valueOf( attributeValue.getDOM().getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "nil" ) ))
            return null;

        String xsType = attributeValue.getDOM().getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "type" );
        String xsValue = attributeValue.getDOM().getTextContent();

        if ("xs:boolean".equals( xsType ))
            return Boolean.valueOf( xsValue );
        else if ("xs:integer".equals( xsType ))
            return Integer.valueOf( xsValue );
        else if ("xs:long".equals( xsType ))
            return Long.valueOf( xsValue );
        else if ("xs:short".equals( xsType ))
            return Short.valueOf( xsValue );
        else if ("xs:byte".equals( xsType ))
            return Byte.valueOf( xsValue );
        else if ("xs:float".equals( xsType ))
            return Float.valueOf( xsValue );
        else if ("xs:double".equals( xsType ))
            return Double.valueOf( xsValue );
        else if ("xs:dateTime".equals( xsType ))
            return new DateTime( xsValue ).toDate();
        else if ("xs:string".equals( xsType ))
            return xsValue;

        throw new IllegalArgumentException(
                "XML Type (xsi:type=" + xsType + ") of attribute value (text=" + xsValue + ") not understood." );
    }
}
