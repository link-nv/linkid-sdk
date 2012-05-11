package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccess;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientApplication;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthValidationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;


/**
 * Checks if the client is properly configured
 * <p/>
 * Date: 11/05/12
 * Time: 11:14
 *
 * @author: sgdesmet
 */
public class ClientValidator extends AbstractValidator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientApplication application)
            throws OauthValidationException {

        checkConfiguration( application );
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

        checkConfiguration( clientApplication );
        checkCorrectMatch( clientAccess, clientApplication);
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

        checkConfiguration( clientApplication );
        checkCorrectMatch( clientAccess, clientApplication);
    }

    protected void checkConfiguration(ClientApplication application) throws OauthValidationException{
        if (application != null){
            if (application.isConfidential() && MessageUtils.stringEmpty( application.getClientSecret() )){
                throw new OauthValidationException( OAuth2Message.ErrorType.SERVER_ERROR,
                        "client " + application.getClientId() + "is not properly configured" );
            }
            if (!application.isConfidential() && MessageUtils.collectionEmpty( application.getRedirectUris() )){
                throw new OauthValidationException( OAuth2Message.ErrorType.SERVER_ERROR,
                        "client " + application.getClientId() + "is not properly configured" );
            }
        }
    }

    protected void checkCorrectMatch(ClientAccess accessRequest, ClientApplication clientApplication)
            throws OauthValidationException {
        if (accessRequest != null && clientApplication != null
            && !clientApplication.getClientId().equals( accessRequest.getClient().getClientId() ) ){
            throw new OauthValidationException( OAuth2Message.ErrorType.INVALID_REQUEST, "token is not meant for this client" );
        }
    }
}
