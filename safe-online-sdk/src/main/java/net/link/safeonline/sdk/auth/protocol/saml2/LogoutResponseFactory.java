/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import java.security.NoSuchAlgorithmException;
import net.link.safeonline.sdk.auth.protocol.LogoutProtocolRequestContext;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.ConfigurationException;


/**
 * Factory for SAML2 logout responses.
 *
 * @author wvdhaute
 */
public class LogoutResponseFactory {

    static {
        /*
         * Next is because Sun loves to endorse crippled versions of Xerces.
         */
        System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            throw new RuntimeException( "could not bootstrap the OpenSAML2 library" );
        }
    }

    private LogoutResponseFactory() {

        // empty
    }

    public static LogoutResponse createLogoutResponse(boolean partialLogout, LogoutProtocolRequestContext logoutRequest, String issuer, String destination) {

        LogoutResponse response = Saml2Util.buildXMLObject( LogoutResponse.class, LogoutResponse.DEFAULT_ELEMENT_NAME );

        DateTime now = new DateTime();

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException( "secure random init error: " + e.getMessage(), e );
        }
        response.setID( idGenerator.generateIdentifier() );
        response.setVersion( SAMLVersion.VERSION_20 );
        response.setInResponseTo( logoutRequest.getId() );
        response.setIssueInstant( now );

        Issuer responseIssuer = Saml2Util.buildXMLObject( Issuer.class, Issuer.DEFAULT_ELEMENT_NAME );
        responseIssuer.setValue( issuer );
        response.setIssuer( responseIssuer );

        response.setDestination( destination );

        Status status = Saml2Util.buildXMLObject( Status.class, Status.DEFAULT_ELEMENT_NAME );
        StatusCode statusCode = Saml2Util.buildXMLObject( StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME );
        if (partialLogout)
            statusCode.setValue( StatusCode.PARTIAL_LOGOUT_URI );
        else
            statusCode.setValue( StatusCode.SUCCESS_URI );
        status.setStatusCode( statusCode );
        response.setStatus( status );

        return response;
    }
}
