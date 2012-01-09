/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.xml.namespace.QName;
import net.link.safeonline.sdk.auth.protocol.saml2.sessiontracking.*;
import net.link.util.saml.Saml2Utils;
import net.link.util.saml.SamlUtils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.*;
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
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        //noinspection HardcodedFileSeparator
        System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
        try {
            DefaultBootstrap.bootstrap();
            Configuration.registerObjectProvider( SessionInfo.DEFAULT_ELEMENT_NAME, new SessionInfoBuilder(), new SessionInfoMarshaller(),
                    new SessionInfoUnmarshaller() );
        }
        catch (ConfigurationException e) {
            throw new RuntimeException( "could not bootstrap the OpenSAML2 library", e );
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
     * @param devices                     the optional list of allowed authentication devices.
     * @param forceAuthentication         whether authentication should be forced and SSO ignore
     * @param sessionTrackingId           optional session info, marks application wishes to track this session
     * @param deviceContext               optional device context, can contain context attributes for specific device's like the iOS client
     *
     * @return unsigned SAML v2.0 AuthnRequest object
     */
    public static AuthnRequest createAuthnRequest(String issuerName, @Nullable List<String> audiences, @Nullable String applicationFriendlyName,
                                                  String assertionConsumerServiceURL, @Nullable String destinationURL, @Nullable Set<String> devices,
                                                  boolean forceAuthentication, @Nullable String sessionTrackingId,
                                                  @Nullable Map<String, String> deviceContext) {

        if (null == issuerName)
            throw new IllegalArgumentException( "application name should not be null" );

        AuthnRequest request = SamlUtils.buildXMLObject( AuthnRequest.DEFAULT_ELEMENT_NAME );

        request.setForceAuthn( forceAuthentication );
        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( "secure random init error: " + e.getMessage(), e );
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

        if (null != devices) {
            RequestedAuthnContext requestedAuthnContext = SamlUtils.buildXMLObject( RequestedAuthnContext.DEFAULT_ELEMENT_NAME );
            List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
            for (String device : devices) {
                AuthnContextClassRef authnContextClassRef = SamlUtils.buildXMLObject( AuthnContextClassRef.DEFAULT_ELEMENT_NAME );
                authnContextClassRef.setAuthnContextClassRef( device );
                authnContextClassRefs.add( authnContextClassRef );
            }
            request.setRequestedAuthnContext( requestedAuthnContext );
        }

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

        // add session info
        if (null != sessionTrackingId) {

            if (null == request.getExtensions()) {
                QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
                Extensions extensions = SamlUtils.buildXMLObject( extensionsQName );
                request.setExtensions( extensions );
            }

            SessionInfo sessionInfo = SamlUtils.buildXMLObject( SessionInfo.DEFAULT_ELEMENT_NAME );
            sessionInfo.setSession( sessionTrackingId );
            request.getExtensions().getUnknownXMLObjects().add( sessionInfo );
        }

        // add device context
        if (null != deviceContext) {

            if (null == request.getExtensions()) {
                QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
                Extensions extensions = SamlUtils.buildXMLObject( extensionsQName );
                request.setExtensions( extensions );
            }

            request.getExtensions().getUnknownXMLObjects().add( toAttributeStatement( deviceContext ) );
        }

        return request;
    }

    /**
     * Returns a SAML v2.0 {@link AttributeStatement} constructed from specified device context.
     *
     * @param deviceContext device context
     *
     * @return attribute statement
     */
    public static AttributeStatement toAttributeStatement(Map<String, String> deviceContext) {

        AttributeStatement attributeStatement = SamlUtils.buildXMLObject( AttributeStatement.DEFAULT_ELEMENT_NAME );

        for (Map.Entry<String, String> entry : deviceContext.entrySet()) {
            Attribute attribute = SamlUtils.buildXMLObject( Attribute.DEFAULT_ELEMENT_NAME );
            attribute.setName( entry.getKey() );
            if (entry.getValue() != null) {
                attribute.getAttributeValues().add( Saml2Utils.toAttributeValue( entry.getValue() ) );
            }
            attributeStatement.getAttributes().add( attribute );
        }

        return attributeStatement;
    }
}
