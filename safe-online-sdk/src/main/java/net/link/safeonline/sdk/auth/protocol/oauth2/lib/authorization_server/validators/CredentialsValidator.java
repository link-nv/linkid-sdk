package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccess;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientApplication;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.AuthorizationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthValidationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Basic credentials validator, performing a string match
 * <p/>
 * Date: 03/05/12
 * Time: 14:49
 *
 * @author: sgdesmet
 */
public class CredentialsValidator extends AbstractValidator {

    static final Log LOG = LogFactory.getLog( CredentialsValidator.class );

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

        //if a client is confidential or has credentials, or the flow used is client credentials, require client authentication
        if ((clientApplication.isConfidential() || !MessageUtils.stringEmpty( clientApplication.getClientSecret() )
                || request.getGrantType().equals( OAuth2Message.GrantType.CLIENT_CREDENTIALS ) )
                && MessageUtils.stringEmpty( request.getClientSecret() ) ) {
            throw new AuthorizationException( "authorization required" );
        }
        checkCredentials( request.getClientId(), request.getClientSecret(), clientAccess );
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

        // don't require credentials, but validate them if they are present
        checkCredentials( request.getClientId(), request.getClientSecret(), clientAccess );
        if ( MessageUtils.stringEmpty( request.getClientId() ) || MessageUtils.stringEmpty( request.getClientSecret() )){
            LOG.warn( "Access Token validation without client credentials present" ); //TODO require credentials here too?
        }
    }

    /**
     * Checks credentials IF they are present. Does not error if they are not present
     * @param clientId
     * @param clientSecret
     * @param clientAccess
     * @throws AuthorizationException
     */
    protected void checkCredentials(String clientId, String clientSecret, final ClientAccess clientAccess )
            throws AuthorizationException {

        if (  (clientId != null && !clientId.equals( clientAccess.getClient().getClientId() ) )
                || (clientSecret != null && !clientSecret.equals( clientAccess.getClient().getClientSecret() ) ) ){
            throw new AuthorizationException( "client authorization failed" );

        }
    }
}
