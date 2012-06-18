package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientConfiguration;
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
    public void validate(final AuthorizationRequest request, final ClientConfiguration configuration)
            throws OauthValidationException {

        checkConfiguration( configuration );
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OauthValidationException {

        checkConfiguration( clientConfiguration );
        checkCorrectMatch( clientAccessRequest, clientConfiguration );
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OauthValidationException {

        checkConfiguration( clientConfiguration );
        checkCorrectMatch( clientAccessRequest, clientConfiguration );
    }

    protected void checkConfiguration(ClientConfiguration configuration) throws OauthValidationException{
        if (configuration != null){
            if (configuration.isConfidential() && MessageUtils.stringEmpty( configuration.getClientSecret() )){
                throw new OauthValidationException( OAuth2Message.ErrorType.SERVER_ERROR,
                        "client " + configuration.getClientId() + "is not properly configured" );
            }
            if (!configuration.isConfidential() && MessageUtils.collectionEmpty( configuration.getRedirectUris() )){
                throw new OauthValidationException( OAuth2Message.ErrorType.SERVER_ERROR,
                        "client " + configuration.getClientId() + "is not properly configured" );
            }
        }
    }

    protected void checkCorrectMatch(ClientAccessRequest accessRequest, ClientConfiguration clientConfiguration)
            throws OauthValidationException {
        if (accessRequest != null && clientConfiguration != null
            && !clientConfiguration.getClientId().equals( accessRequest.getClient().getClientId() ) ){
            throw new OauthValidationException( OAuth2Message.ErrorType.INVALID_REQUEST, "token is not meant for this client" );
        }
    }
}
