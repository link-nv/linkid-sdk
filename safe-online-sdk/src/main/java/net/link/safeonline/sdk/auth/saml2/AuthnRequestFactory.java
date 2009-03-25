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

import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.saml.common.Saml2Util;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.xml.ConfigurationException;


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
     * Creates a SAML2 authentication request. For the moment we allow the Service Provider to pass on the Assertion Consumer Service URL
     * itself. Later on we could use the SAML Metadata service or a persistent server-side application field to locate this service.
     * 
     * @param issuerName
     * @param audiences
     *            the optional list of audiences is the optional list of application pools that can be specified for use in Single Sign On
     * @param applicationFriendlyName
     * @param signerKeyPair
     * @param assertionConsumerServiceURL
     *            the optional location of the assertion consumer service. This location can be used by the IdP to send back the SAML
     *            response message.
     * @param destinationURL
     *            the optional location of the destination IdP.
     * @param challenge
     *            the optional challenge (output variable).
     * @param devices
     *            the optional list of allowed authentication devices.
     */
    public static String createAuthnRequest(String issuerName, List<String> audiences, String applicationFriendlyName,
                                            KeyPair signerKeyPair, String assertionConsumerServiceURL, String destinationURL,
                                            Challenge<String> challenge, Set<String> devices, boolean ssoEnabled) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("application name should not be null");

        AuthnRequest request = Saml2Util.buildXMLObject(AuthnRequest.class, AuthnRequest.DEFAULT_ELEMENT_NAME);

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
        Issuer issuer = Saml2Util.buildXMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(issuerName);
        request.setIssuer(issuer);

        if (null != assertionConsumerServiceURL) {
            request.setAssertionConsumerServiceURL(assertionConsumerServiceURL);
            request.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        }

        if (null != destinationURL) {
            request.setDestination(destinationURL);
        }

        if (null != applicationFriendlyName) {
            request.setProviderName(applicationFriendlyName);
        }

        NameIDPolicy nameIdPolicy = Saml2Util.buildXMLObject(NameIDPolicy.class, NameIDPolicy.DEFAULT_ELEMENT_NAME);
        nameIdPolicy.setAllowCreate(true);
        request.setNameIDPolicy(nameIdPolicy);

        if (null != devices) {
            RequestedAuthnContext requestedAuthnContext = Saml2Util.buildXMLObject(RequestedAuthnContext.class,
                    RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
            List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
            for (String device : devices) {
                AuthnContextClassRef authnContextClassRef = Saml2Util.buildXMLObject(AuthnContextClassRef.class,
                        AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
                authnContextClassRef.setAuthnContextClassRef(device);
                authnContextClassRefs.add(authnContextClassRef);
            }
            request.setRequestedAuthnContext(requestedAuthnContext);
        }

        if (null != audiences) {
            Conditions conditions = Saml2Util.buildXMLObject(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
            List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
            AudienceRestriction audienceRestriction = Saml2Util.buildXMLObject(AudienceRestriction.class,
                    AudienceRestriction.DEFAULT_ELEMENT_NAME);
            audienceRestrictions.add(audienceRestriction);
            List<Audience> audienceList = audienceRestriction.getAudiences();
            for (String audienceName : audiences) {
                Audience audience = Saml2Util.buildXMLObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
                audienceList.add(audience);
                audience.setAudienceURI(audienceName);
            }
            request.setConditions(conditions);
        }

        return Saml2Util.sign(request, signerKeyPair);
    }
}
