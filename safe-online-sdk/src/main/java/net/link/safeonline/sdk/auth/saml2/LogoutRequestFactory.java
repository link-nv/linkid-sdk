/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import net.link.safeonline.saml.common.Challenge;
import net.link.safeonline.saml.common.Saml2Util;

import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.xml.ConfigurationException;


/**
 * Factory class for SAML2 logout requests.
 * 
 * <p>
 * We're using the OpenSAML2 Java library for construction of the XML SAML documents.
 * </p>
 * 
 * @author wvdhaute
 * 
 */
public class LogoutRequestFactory {

    private LogoutRequestFactory() {

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
     * Creates a SAML2 logout request. For the moment we allow the Service Provider to pass on the Assertion Consumer Service URL itself.
     * Later on we could use the SAML Metadata service or a persistent server-side application field to locate this service.
     * 
     * @param subjectName
     * @param issuerName
     * @param signerKeyPair
     * @param destinationURL
     *            the optional location of the destination IdP.
     * @param challenge
     *            the optional challenge (output variable).
     */
    public static String createLogoutRequest(String subjectName, String issuerName, KeyPair signerKeyPair, String destinationURL,
                                             Challenge<String> challenge) {

        if (null == signerKeyPair)
            throw new IllegalArgumentException("signer key pair should not be null");
        if (null == issuerName)
            throw new IllegalArgumentException("application name should not be null");

        LogoutRequest request = Saml2Util.buildXMLObject(LogoutRequest.class, LogoutRequest.DEFAULT_ELEMENT_NAME);

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

        if (null != destinationURL) {
            request.setDestination(destinationURL);
        }

        NameID nameID = Saml2Util.buildXMLObject(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue(subjectName);
        nameID.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        request.setNameID(nameID);

        return Saml2Util.sign(request, signerKeyPair);
    }

}
