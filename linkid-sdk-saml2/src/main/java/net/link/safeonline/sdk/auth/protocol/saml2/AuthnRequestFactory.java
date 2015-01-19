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
import net.link.safeonline.sdk.api.callback.CallbackDO;
import net.link.safeonline.sdk.api.payment.PaymentContextDO;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.Callback;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.CallbackBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.CallbackMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.callback.CallbackUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.DeviceContext;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.DeviceContextBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.DeviceContextMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.devicecontext.DeviceContextUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.ExternalCodeResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.ExternalCodeResponseBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.ExternalCodeResponseMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.ExternalCodeResponseUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.PaymentContext;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.PaymentContextBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.PaymentContextMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext.PaymentContextUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.PaymentResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.PaymentResponseBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.PaymentResponseMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.PaymentResponseUnmarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.SubjectAttributes;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.SubjectAttributesBuilder;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.SubjectAttributesMarshaller;
import net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes.SubjectAttributesUnmarshaller;
import net.link.util.InternalInconsistencyException;
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
public class AuthnRequestFactory {

    private AuthnRequestFactory() {

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
            DefaultBootstrap.bootstrap();
            Configuration.registerObjectProvider( DeviceContext.DEFAULT_ELEMENT_NAME, new DeviceContextBuilder(), new DeviceContextMarshaller(),
                    new DeviceContextUnmarshaller() );
            Configuration.registerObjectProvider( SubjectAttributes.DEFAULT_ELEMENT_NAME, new SubjectAttributesBuilder(), new SubjectAttributesMarshaller(),
                    new SubjectAttributesUnmarshaller() );
            Configuration.registerObjectProvider( PaymentContext.DEFAULT_ELEMENT_NAME, new PaymentContextBuilder(), new PaymentContextMarshaller(),
                    new PaymentContextUnmarshaller() );
            Configuration.registerObjectProvider( PaymentResponse.DEFAULT_ELEMENT_NAME, new PaymentResponseBuilder(), new PaymentResponseMarshaller(),
                    new PaymentResponseUnmarshaller() );
            Configuration.registerObjectProvider( Callback.DEFAULT_ELEMENT_NAME, new CallbackBuilder(), new CallbackMarshaller(), new CallbackUnmarshaller() );
            Configuration.registerObjectProvider( ExternalCodeResponse.DEFAULT_ELEMENT_NAME, new ExternalCodeResponseBuilder(),
                    new ExternalCodeResponseMarshaller(), new ExternalCodeResponseUnmarshaller() );
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
     * @param callbackDO                  optional callback config for when the linkID auth/payment has finished
     *
     * @return unsigned SAML v2.0 AuthnRequest object
     */
    public static AuthnRequest createAuthnRequest(String issuerName, @Nullable List<String> audiences, @Nullable String applicationFriendlyName,
                                                  String assertionConsumerServiceURL, @Nullable String destinationURL, boolean forceAuthentication,
                                                  @Nullable Map<String, String> deviceContextMap,
                                                  @Nullable Map<String, List<Serializable>> subjectAttributesMap, @Nullable PaymentContextDO paymentContext,
                                                  @Nullable CallbackDO callbackDO) {

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
        if (null != callbackDO) {

            if (null == request.getExtensions()) {
                QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
                Extensions extensions = SamlUtils.buildXMLObject( extensionsQName );
                request.setExtensions( extensions );
            }

            request.getExtensions().getUnknownXMLObjects().add( toCallback( callbackDO ) );
        }

        return request;
    }

    /**
     * Returns a SAML v2.0 {@link PaymentContext} constructed from specified payment context
     *
     * @param paymentContextDO the payment context
     *
     * @return PaymentContext SAML v2.0 object.
     */
    private static PaymentContext toPaymentContext(final PaymentContextDO paymentContextDO) {

        PaymentContext paymentContext = SamlUtils.buildXMLObject( PaymentContext.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, String> entry : paymentContextDO.toMap().entrySet()) {
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
     * Returns a SAML v2.0 {@link DeviceContext} constructed from specified device context.
     *
     * @param deviceContextMap device context map
     *
     * @return DeviceContext SAML v2.0 object.
     */
    public static DeviceContext toDeviceContext(Map<String, String> deviceContextMap) {

        DeviceContext deviceContext = SamlUtils.buildXMLObject( DeviceContext.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, String> entry : deviceContextMap.entrySet()) {
            Attribute attribute = SamlUtils.buildXMLObject( Attribute.DEFAULT_ELEMENT_NAME );
            attribute.setName( entry.getKey() );
            if (entry.getValue() != null) {
                attribute.getAttributeValues().add( Saml2Utils.toAttributeValue( entry.getValue() ) );
            }
            deviceContext.getAttributes().add( attribute );
        }

        return deviceContext;
    }

    /**
     * Returns a SAML v2.0 {@link SubjectAttributes} constructed from specified subject attributes map.
     *
     * @param subjectAttributesMap subject attributes map
     *
     * @return SubjectAttributes SAML v2.0 object.
     */
    public static SubjectAttributes toSubjectAttributes(Map<String, List<Serializable>> subjectAttributesMap) {

        SubjectAttributes subjectAttributes = SamlUtils.buildXMLObject( SubjectAttributes.DEFAULT_ELEMENT_NAME );

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
     * Returns a SAML v2.0 {@link Callback} constructed from specified callback DO
     *
     * @param callbackDO the callback config
     *
     * @return Callback SAML v2.0 object.
     */
    private static Callback toCallback(final CallbackDO callbackDO) {

        Callback callback = SamlUtils.buildXMLObject( Callback.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, String> entry : callbackDO.toMap().entrySet()) {
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
