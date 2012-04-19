/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import java.security.NoSuchAlgorithmException;
import net.link.safeonline.sdk.auth.protocol.LogoutProtocolRequestContext;
import net.link.util.saml.SamlUtils;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.*;


/**
 * Factory for SAML2 logout responses.
 *
 * @author wvdhaute
 */
public class LogoutResponseFactory {

    static {
        AuthnRequestFactory.bootstrapSaml2();
    }

    private LogoutResponseFactory() {

        // empty
    }

    public static LogoutResponse createLogoutResponse(boolean partialLogout, LogoutProtocolRequestContext logoutRequest, String issuer,
                                                      String destination) {

        LogoutResponse response = SamlUtils.buildXMLObject( LogoutResponse.DEFAULT_ELEMENT_NAME );

        DateTime now = new DateTime();

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalInconsistencyException( String.format( "secure random init error: %s", e.getMessage() ), e );
        }
        response.setID( idGenerator.generateIdentifier() );
        response.setVersion( SAMLVersion.VERSION_20 );
        response.setInResponseTo( logoutRequest.getId() );
        response.setIssueInstant( now );

        Issuer responseIssuer = SamlUtils.buildXMLObject( Issuer.DEFAULT_ELEMENT_NAME );
        responseIssuer.setValue( issuer );
        response.setIssuer( responseIssuer );

        response.setDestination( destination );

        Status status = SamlUtils.buildXMLObject( Status.DEFAULT_ELEMENT_NAME );
        StatusCode statusCode = SamlUtils.buildXMLObject( StatusCode.DEFAULT_ELEMENT_NAME );
        if (partialLogout)
            statusCode.setValue( StatusCode.PARTIAL_LOGOUT_URI );
        else
            statusCode.setValue( StatusCode.SUCCESS_URI );
        status.setStatusCode( statusCode );
        response.setStatus( status );

        return response;
    }
}
