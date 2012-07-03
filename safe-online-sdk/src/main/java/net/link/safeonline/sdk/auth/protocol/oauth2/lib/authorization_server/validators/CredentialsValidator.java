package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OAuthAuthorizationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Basic credentials validator, performing a string match
 * <p/>
 * Date: 03/05/12
 * Time: 14:49
 *
 * @author sgdesmet
 */
public class CredentialsValidator extends AbstractValidator {

    static final Log LOG = LogFactory.getLog( CredentialsValidator.class );

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest,
                         final ClientConfiguration clientConfiguration)
            throws OAuthException {

        //if a client is confidential or has credentials, or the flow used is client credentials, require client authentication
        if ((clientConfiguration.isConfidential() || !MessageUtils.stringEmpty( clientConfiguration.getClientSecret() )
             || request.getGrantType() == OAuth2Message.GrantType.CLIENT_CREDENTIALS) && MessageUtils.stringEmpty(
                request.getClientSecret() )) {
            throw new OAuthAuthorizationException(
                    String.format( "Authorization required for client %s", clientAccessRequest.getClient().getClientId() ) );
        }
        validateCredentials( request.getClientId(), request.getClientSecret(), clientAccessRequest );
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccessRequest clientAccessRequest,
                         final ClientConfiguration clientConfiguration)
            throws OAuthException {

        // don't require credentials, but validate them if they are present
        validateCredentials( request.getClientId(), request.getClientSecret(), clientAccessRequest );
        if (MessageUtils.stringEmpty( request.getClientId() ) || MessageUtils.stringEmpty( request.getClientSecret() )) {
            LOG.warn( "Access Token validation without client credentials present" ); //TODO require credentials here too?
        }
    }

    /**
     * Checks credentials IF they are present. Does not error if they are not present
     */
    protected void validateCredentials(String clientId, String clientSecret, final ClientAccessRequest clientAccessRequest)
            throws OAuthAuthorizationException {

        if (clientId != null && !clientId.equals( clientAccessRequest.getClient().getClientId() )
            || clientSecret != null && !clientSecret.equals( clientAccessRequest.getClient().getClientSecret() )) {
            throw new OAuthAuthorizationException( String.format( "Client authorization failed for client %s", clientId ) );
        }
    }
}
