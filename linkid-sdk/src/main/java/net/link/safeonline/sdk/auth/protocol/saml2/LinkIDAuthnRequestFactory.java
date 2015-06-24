/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.LinkIDCallback;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.LinkIDCallbackBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.LinkIDCallbackMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.LinkIDCallbackUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.LinkIDDeviceContext;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.LinkIDDeviceContextBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.LinkIDDeviceContextMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.LinkIDDeviceContextUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.LinkIDExternalCodeResponseBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.LinkIDExternalCodeResponseMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.LinkIDExternalCodeResponseUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.LinkIDPaymentContext;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.LinkIDPaymentContextBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.LinkIDPaymentContextMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.LinkIDPaymentContextUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.LinkIDPaymentResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.LinkIDPaymentResponseBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.LinkIDPaymentResponseMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.LinkIDPaymentResponseUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.LinkIDSubjectAttributes;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.LinkIDSubjectAttributesBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.LinkIDSubjectAttributesMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.LinkIDSubjectAttributesUnmarshaller;
import net.link.util.InternalInconsistencyException;
import net.link.util.logging.Logger;
import net.link.util.saml.Saml2Utils;
import net.link.util.saml.SamlUtils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;


/**
 * Factory class for SAML2 authentication requests.
 * <p/>
 * <p>
 * We're using the OpenSAML2 Java library for construction of the XML SAML documents.
 * </p>
 *
 * @author fcorneli
 */
public class LinkIDAuthnRequestFactory {

    private static final Logger logger = Logger.get( LinkIDAuthnRequestFactory.class );

    private LinkIDAuthnRequestFactory() {

        // empty
    }

    static {

        bootstrapSaml2();
    }

    public static void bootstrapSaml2() {


        /*
        * Next is because Sun loves to endorse crippled versions of Xerces.
        */
        //noinspection HardcodedFileSeparator
        System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema", "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
        try {
            if (Configuration.getParserPool() == null) {

                logger.inf( "Bootstrap SAML2" );

                DefaultBootstrap.bootstrap();
                Configuration.registerObjectProvider( LinkIDDeviceContext.DEFAULT_ELEMENT_NAME, new LinkIDDeviceContextBuilder(),
                        new LinkIDDeviceContextMarshaller(), new LinkIDDeviceContextUnmarshaller() );
                Configuration.registerObjectProvider( LinkIDSubjectAttributes.DEFAULT_ELEMENT_NAME, new LinkIDSubjectAttributesBuilder(),
                        new LinkIDSubjectAttributesMarshaller(), new LinkIDSubjectAttributesUnmarshaller() );
                Configuration.registerObjectProvider( LinkIDPaymentContext.DEFAULT_ELEMENT_NAME, new LinkIDPaymentContextBuilder(),
                        new LinkIDPaymentContextMarshaller(), new LinkIDPaymentContextUnmarshaller() );
                Configuration.registerObjectProvider( LinkIDPaymentResponse.DEFAULT_ELEMENT_NAME, new LinkIDPaymentResponseBuilder(),
                        new LinkIDPaymentResponseMarshaller(), new LinkIDPaymentResponseUnmarshaller() );
                Configuration.registerObjectProvider( LinkIDCallback.DEFAULT_ELEMENT_NAME, new LinkIDCallbackBuilder(), new LinkIDCallbackMarshaller(),
                        new LinkIDCallbackUnmarshaller() );
                Configuration.registerObjectProvider( LinkIDExternalCodeResponse.DEFAULT_ELEMENT_NAME, new LinkIDExternalCodeResponseBuilder(),
                        new LinkIDExternalCodeResponseMarshaller(), new LinkIDExternalCodeResponseUnmarshaller() );
            }
        }
        catch (ConfigurationException e) {
            throw new InternalInconsistencyException( "could not bootstrap the OpenSAML2 library", e );
        }
    }

    /**
     * Creates a SAML2 authentication request. For the moment we allow the Service Provider to pass on the Assertion Consumer Service URL
     * itself. Later on we could use the SAML Metadata service or a persistent server-side application field to locate this service.
     *
     * @param issuerName                  issuer of the authentication request
     * @param audiences                   the optional list of audiences is the optional list of application pools that can be specified
     *                                    for
     *                                    use in Single Sign On
     * @param applicationFriendlyName     optional application friendly name to be displayed by linkID
     * @param assertionConsumerServiceURL the optional location of the assertion consumer service. This location can be used by the IdP to
     *                                    send back the SAML
     *                                    response message.
     * @param destinationURL              the optional location of the destination IdP.
     * @param forceAuthentication         whether authentication should be forced and SSO ignore
     * @param deviceContextMap            optional device context, can contain context attributes for specific device's like the iOS client
     * @param subjectAttributesMap        optional map attributes of the to be authenticated subject. These values will be used if needed
     *                                    in case of missing attributes in the linkID authentication flow. The key's are the linkID
     *                                    attribute names.
     * @param paymentContext              optional payment context case the authentication serves as a payment request.
     * @param linkIDCallback              optional callback config for when the linkID auth/payment has finished
     *
     * @return unsigned SAML v2.0 AuthnRequest object
     */
    public static AuthnRequest createAuthnRequest(String issuerName, @Nullable List<String> audiences, @Nullable String applicationFriendlyName,
                                                  String assertionConsumerServiceURL, @Nullable String destinationURL, boolean forceAuthentication,
                                                  @Nullable Map<String, String> deviceContextMap,
                                                  @Nullable Map<String, List<Serializable>> subjectAttributesMap,
                                                  @Nullable net.link.safeonline.sdk.api.payment.LinkIDPaymentContext paymentContext,
                                                  @Nullable net.link.safeonline.sdk.api.callback.LinkIDCallback linkIDCallback) {

        if (null == issuerName)
            throw new IllegalArgumentException( "application name should not be null" );

        AuthnRequest request = SamlUtils.buildXMLObject( AuthnRequest.DEFAULT_ELEMENT_NAME );

        request.setForceAuthn( forceAuthentication );
        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( String.format( "secure random init error: %s", e.getMessage() ), e );
        }
        String id = idGenerator.generateIdentifier();
        request.setID( id );
        request.setVersion( SAMLVersion.VERSION_20 );
        request.setIssueInstant( new DateTime() );
        Issuer issuer = SamlUtils.buildXMLObject( Issuer.DEFAULT_ELEMENT_NAME );
        issuer.setValue( issuerName );
        request.setIssuer( issuer );

        if (null != assertionConsumerServiceURL) {
            request.setAssertionConsumerServiceURL( assertionConsumerServiceURL );
            request.setProtocolBinding( SAMLConstants.SAML2_POST_BINDING_URI );
        }

        if (null != destinationURL)
            request.setDestination( destinationURL );

        if (null != applicationFriendlyName)
            request.setProviderName( applicationFriendlyName );

        NameIDPolicy nameIdPolicy = SamlUtils.buildXMLObject( NameIDPolicy.DEFAULT_ELEMENT_NAME );
        nameIdPolicy.setAllowCreate( true );
        request.setNameIDPolicy( nameIdPolicy );

        if (null != audiences) {
            Conditions conditions = SamlUtils.buildXMLObject( Conditions.DEFAULT_ELEMENT_NAME );
            List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
            AudienceRestriction audienceRestriction = SamlUtils.buildXMLObject( AudienceRestriction.DEFAULT_ELEMENT_NAME );
            audienceRestrictions.add( audienceRestriction );
            List<Audience> audienceList = audienceRestriction.getAudiences();
            for (String audienceName : audiences) {
                Audience audience = SamlUtils.buildXMLObject( Audience.DEFAULT_ELEMENT_NAME );
                audienceList.add( audience );
                audience.setAudienceURI( audienceName );
            }
            request.setConditions( conditions );
        }

        // add device context
        if (null != deviceContextMap) {

            if (null == request.getExtensions()) {
                QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
                Extensions extensions = SamlUtils.buildXMLObject( extensionsQName );
                request.setExtensions( extensions );
            }

            request.getExtensions().getUnknownXMLObjects().add( toDeviceContext( deviceContextMap ) );
        }

        // add attributes
        if (null != subjectAttributesMap) {

            if (null == request.getExtensions()) {
                QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
                Extensions extensions = SamlUtils.buildXMLObject( extensionsQName );
                request.setExtensions( extensions );
            }

            request.getExtensions().getUnknownXMLObjects().add( toSubjectAttributes( subjectAttributesMap ) );
        }

        // add payment context
        if (null != paymentContext) {

            if (null == request.getExtensions()) {
                QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
                Extensions extensions = SamlUtils.buildXMLObject( extensionsQName );
                request.setExtensions( extensions );
            }

            request.getExtensions().getUnknownXMLObjects().add( toPaymentContext( paymentContext ) );
        }

        // add callback
        if (null != linkIDCallback) {

            if (null == request.getExtensions()) {
                QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
                Extensions extensions = SamlUtils.buildXMLObject( extensionsQName );
                request.setExtensions( extensions );
            }

            request.getExtensions().getUnknownXMLObjects().add( toCallback( linkIDCallback ) );
        }

        return request;
    }

    /**
     * Returns a SAML v2.0 {@link LinkIDPaymentContext} constructed from specified payment context
     *
     * @param linkIDPaymentContext the payment context
     *
     * @return PaymentContext SAML v2.0 object.
     */
    private static LinkIDPaymentContext toPaymentContext(final net.link.safeonline.sdk.api.payment.LinkIDPaymentContext linkIDPaymentContext) {

        LinkIDPaymentContext paymentContext = SamlUtils.buildXMLObject( LinkIDPaymentContext.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, String> entry : linkIDPaymentContext.toMap().entrySet()) {
            Attribute attribute = SamlUtils.buildXMLObject( Attribute.DEFAULT_ELEMENT_NAME );
            attribute.setName( entry.getKey() );
            if (entry.getValue() != null) {
                attribute.getAttributeValues().add( Saml2Utils.toAttributeValue( entry.getValue() ) );
            }
            paymentContext.getAttributes().add( attribute );
        }

        return paymentContext;
    }

    /**
     * Returns a SAML v2.0 {@link LinkIDDeviceContext} constructed from specified device context.
     *
     * @param deviceContextMap device context map
     *
     * @return DeviceContext SAML v2.0 object.
     */
    public static LinkIDDeviceContext toDeviceContext(Map<String, String> deviceContextMap) {

        LinkIDDeviceContext linkIDDeviceContext = SamlUtils.buildXMLObject( LinkIDDeviceContext.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, String> entry : deviceContextMap.entrySet()) {
            Attribute attribute = SamlUtils.buildXMLObject( Attribute.DEFAULT_ELEMENT_NAME );
            attribute.setName( entry.getKey() );
            if (entry.getValue() != null) {
                attribute.getAttributeValues().add( Saml2Utils.toAttributeValue( entry.getValue() ) );
            }
            linkIDDeviceContext.getAttributes().add( attribute );
        }

        return linkIDDeviceContext;
    }

    /**
     * Returns a SAML v2.0 {@link LinkIDSubjectAttributes} constructed from specified subject attributes map.
     *
     * @param subjectAttributesMap subject attributes map
     *
     * @return SubjectAttributes SAML v2.0 object.
     */
    public static LinkIDSubjectAttributes toSubjectAttributes(Map<String, List<Serializable>> subjectAttributesMap) {

        LinkIDSubjectAttributes subjectAttributes = SamlUtils.buildXMLObject( LinkIDSubjectAttributes.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, List<Serializable>> entry : subjectAttributesMap.entrySet()) {

            List<Serializable> values = entry.getValue();
            if (null != values) {

                Attribute attribute = SamlUtils.buildXMLObject( Attribute.DEFAULT_ELEMENT_NAME );
                attribute.setName( entry.getKey() );

                for (Serializable value : values) {

                    attribute.getAttributeValues().add( Saml2Utils.toAttributeValue( value ) );
                }

                subjectAttributes.getAttributes().add( attribute );
            }
        }

        return subjectAttributes;
    }

    /**
     * Returns a SAML v2.0 {@link LinkIDCallback} constructed from specified callback DO
     *
     * @param linkIDCallback the callback config
     *
     * @return Callback SAML v2.0 object.
     */
    private static LinkIDCallback toCallback(final net.link.safeonline.sdk.api.callback.LinkIDCallback linkIDCallback) {

        LinkIDCallback callback = SamlUtils.buildXMLObject( LinkIDCallback.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, String> entry : linkIDCallback.toMap().entrySet()) {
            Attribute attribute = SamlUtils.buildXMLObject( Attribute.DEFAULT_ELEMENT_NAME );
            attribute.setName( entry.getKey() );
            if (entry.getValue() != null) {
                attribute.getAttributeValues().add( Saml2Utils.toAttributeValue( entry.getValue() ) );
            }
            callback.getAttributes().add( attribute );
        }

        return callback;
    }
}
