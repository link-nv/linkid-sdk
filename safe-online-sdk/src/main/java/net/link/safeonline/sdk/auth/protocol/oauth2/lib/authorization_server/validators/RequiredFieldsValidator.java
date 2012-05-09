package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccess;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientApplication;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthValidationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;


/**
 * TODO description
 * <p/>
 * Date: 03/05/12
 * Time: 13:37
 *
 * @author: sgdesmet
 */
public class RequiredFieldsValidator extends AbstractValidator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientApplication application)
            throws OauthValidationException {
        requiredField( request.getResponseType(), OAuth2Message.RESPONSE_TYPE );
        requiredField( request.getClientId(), OAuth2Message.CLIENT_ID );
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {
        requiredField( request.getGrantType(), OAuth2Message.GRANT_TYPE );
        requiredField( request.getClientId(), OAuth2Message.CLIENT_ID );

        //let Credentialsvalidator handle this
//        if (clientApplication.isConfidential() || !MessageUtils.stringEmpty( clientApplication.getClientSecret() ) ){
//            requiredField( request.getClientSecret(), OAuth2Message.CLIENT_SECRET );
//        }

        switch ( request.getGrantType() ){
            case AUTHORIZATION_CODE:
                if (clientAccess == null)
                    throw new OauthValidationException( OAuth2Message.ErrorType.UNAUTHORIZED_CLIENT, "no client request present");
                requiredField( request.getCode(), OAuth2Message.CODE );
                if ( !MessageUtils.stringEmpty( clientAccess.getValidatedRedirectionURI() ) ){
                    requiredField( request.getRedirectUri(), OAuth2Message.REDIRECT_URI );
                }
                break;
            case CLIENT_CREDENTIALS:
                // let Credentialsvalidator handle this
                //requiredField( request.getClientSecret(), OAuth2Message.CLIENT_SECRET );
                break;
            case PASSWORD:
                requiredField( request.getUsername(), OAuth2Message.USERNAME );
                requiredField( request.getPassword(), OAuth2Message.PASSWORD );
                break;
            case REFRESH_TOKEN:
                requiredField( request.getRefreshToken(), OAuth2Message.REFRESH_TOKEN );
                break;
        }
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

        requiredField( request.getAccessToken(), OAuth2Message.ACCESS_TOKEN );
    }
}
