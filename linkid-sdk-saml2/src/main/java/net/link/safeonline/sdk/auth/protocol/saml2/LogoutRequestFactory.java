/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import java.security.NoSuchAlgorithmException;
import net.link.util.InternalInconsistencyException;
import net.link.util.saml.SamlUtils;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;


/**
 * Factory class for SAML2 logout requests.
 * <p/>
 * <p>
 * We're using the OpenSAML2 Java library for construction of the XML SAML documents.
 * </p>
 *
 * @author wvdhaute
 */
public class LogoutRequestFactory {

    private LogoutRequestFactory() {

        // empty
    }

    static {
        AuthnRequestFactory.bootstrapSaml2();
    }

    /**
     * Creates a SAML2 logout request. For the moment we allow the Service Provider to pass on the Assertion Consumer Service URL itself.
     * Later on we could use the SAML Metadata service or a persistent server-side application field to locate this service.
     *
     * @param destinationURL the optional location of the destination IdP.
     */
    public static LogoutRequest createLogoutRequest(String subjectName, String issuerName, String destinationURL) {

        if (null == issuerName)
            throw new IllegalArgumentException( "application name should not be null" );

        LogoutRequest request = SamlUtils.buildXMLObject( LogoutRequest.DEFAULT_ELEMENT_NAME );

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

        if (null != destinationURL)
            request.setDestination( destinationURL );

        NameID nameID = SamlUtils.buildXMLObject( NameID.DEFAULT_ELEMENT_NAME );
        nameID.setValue( subjectName );
        nameID.setFormat( "urn:oasis:names:tc:SAML:2.0:nameid-format:entity" );
        request.setNameID( nameID );

        return request;
    }
}
