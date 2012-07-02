package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import java.util.Date;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.*;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.AccessTokenRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.ValidationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Validates tokens (authorization code or access token) in the request
 * <p/>
 * Date: 03/05/12
 * Time: 14:44
 *
 * @author: sgdesmet
 */
public class TokenRequestValidator extends AbstractValidator {

    static final Log LOG = LogFactory.getLog( TokenRequestValidator.class );

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

        if (clientAccessRequest != null && clientAccessRequest.getUserDefinedExpirationDate() != null && clientAccessRequest.getUserDefinedExpirationDate()
                                                                                                       .before( new Date() )) {
            throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "user authorization expired or revoked" );
        }

        switch ( request.getGrantType() ){
            case AUTHORIZATION_CODE:
                // see if auth grant code has not yet expired, is not already used, or is invoked
                if ( clientAccessRequest == null )
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT, "missing authorization grant" );
                if (!clientAccessRequest.getAuthorizationCode().getTokenData().equals( request.getCode() )
                    || clientAccessRequest.getAuthorizationCode().isInvalid()
                    || clientAccessRequest.getAuthorizationCode().getExpirationDate().before( new Date() )){

                    LOG.error( "ATTENTION: Attempt detected to get an access token using an invalid authorization code: "
                               + clientAccessRequest.getAuthorizationCode() );
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT,
                            "authorization grant is invalid, expired, revoked or already used" );
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
                for (RefreshToken refreshToken : clientAccessRequest.getRefreshTokens()){
                    if (refreshToken.getTokenData().equals( request.getRefreshToken() ) )
                        if (refreshToken.isInvalid() || ( refreshToken.getExpirationDate() != null
                                                           && refreshToken.getExpirationDate().before( new Date(  ) ) ) ) {

                            //invalid token, throw error
                            LOG.error( "ATTENTION: Attempt detected to get an access token using an invalid refresh token: "
                                       + request.getRefreshToken() );
                            throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT,
                                    "refresh token is invalid, expired, revoked or already used" );
                        } else {
                            // token is ok, hurah
                            validToken = true;
                        }
                }
                if (!validToken){
                    // we didn't find that particular token, it would seem
                    LOG.error( "ATTENTION: Attempt detected to get an access token using an invalid refresh token: "
                               + request.getRefreshToken() );
                    throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT,
                            "refresh token is invalid, expired, revoked or already used" );
                }
                break;
        }
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

        boolean valid = false;
        for (AccessToken token : clientAccessRequest.getAccessTokens()){
            if ( request.getAccessToken().equals( token.getTokenData() )
                 && token.getExpirationDate().after( new Date(  ) )
                 && !token.isInvalid()){
                valid = true;
            }
        }
        if (!valid){
            LOG.error( "ATTENTION: invalid access token used: "
                       + request.getAccessToken() );
            throw new OAuthException( OAuth2Message.ErrorType.INVALID_GRANT );
        }
    }
}
