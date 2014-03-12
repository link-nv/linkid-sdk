/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.authorization_server.validators;

import net.link.util.logging.Logger;
import java.util.Date;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.*;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.AccessTokenRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.ValidationRequest;


/**
 * Validates tokens (authorization code or access token) in the request
 * <p/>
 * Date: 03/05/12
 * Time: 14:44
 *
 * @author sgdesmet
 */
public class TokenRequestValidator extends AbstractValidator {

    private static final Logger logger = Logger.get( TokenRequestValidator.class );

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

        if (clientAccessRequest != null && clientAccessRequest.getUserDefinedExpirationDate() != null && clientAccessRequest.getUserDefinedExpirationDate()
                                                                                                                            .before( new Date() )) {
            throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "user authorization expired or revoked" );
        }

        switch (request.getGrantType()) {
            case AUTHORIZATION_CODE:
                // see if auth grant code has not yet expired, is not already used, or is invoked
                if (clientAccessRequest == null)
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "missing authorization grant" );
                if (!clientAccessRequest.getAuthorizationCode().getTokenData().equals( request.getCode() ) || clientAccessRequest.getAuthorizationCode()
                                                                                                                                 .isInvalid()
                    || clientAccessRequest.getAuthorizationCode().getExpirationDate().before( new Date() )) {

                    logger.err( "ATTENTION: Attempt detected to get an access token using an invalid authorization code: %s",
                            clientAccessRequest.getAuthorizationCode() );
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "authorization grant is invalid, expired, revoked or already used" );
                }
                break;
            case CLIENT_CREDENTIALS:
                break;
            case PASSWORD:
                throw new UnsupportedOperationException( "not yet implemented" ); //TODO
            case REFRESH_TOKEN:
                if (clientAccessRequest == null)
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "missing authorization grant" );
                // see if auth grant code has not yet expired, is not already used, or is invoked
                // note: check _all_ tokens
                Boolean validToken = false;
                for (RefreshToken refreshToken : clientAccessRequest.getRefreshTokens()) {
                    if (refreshToken.getTokenData().equals( request.getRefreshToken() ))
                        if (refreshToken.isInvalid() || (refreshToken.getExpirationDate() != null && refreshToken.getExpirationDate().before( new Date() ))) {

                            //invalid token, throw error
                            logger.err( "ATTENTION: Attempt detected to get an access token using an invalid refresh token: %s", request.getRefreshToken() );
                            throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "refresh token is invalid, expired, revoked or already used" );
                        } else {
                            // token is ok, hurah
                            validToken = true;
                        }
                }
                if (!validToken) {
                    // we didn't find that particular token, it would seem
                    logger.err( "ATTENTION: Attempt detected to get an access token using an invalid refresh token: %s", request.getRefreshToken() );
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "refresh token is invalid, expired, revoked or already used" );
                }
                break;
        }
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

        boolean valid = false;
        for (AccessToken token : clientAccessRequest.getAccessTokens()) {
            if (request.getAccessToken().equals( token.getTokenData() ) && token.getExpirationDate().after( new Date() ) && !token.isInvalid()) {
                valid = true;
            }
        }
        if (!valid) {
            logger.err( "ATTENTION: invalid access token used: %s", request.getAccessToken() );
            throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT );
        }
    }
}
