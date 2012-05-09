package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccess;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientApplication;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthValidationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.*;


/**
 * Validate redirection URIs
 * <p/>
 * Date: 22/03/12
 * Time: 17:16
 *
 * @author: sgdesmet
 */
public interface Validator {

    public void validate(AuthorizationRequest request, ClientApplication application) throws OauthValidationException;

    public void validate(AccessTokenRequest request, ClientAccess clientAccess, final ClientApplication clientApplication) throws OauthValidationException;

    public void validate(ValidationRequest request, ClientAccess clientAccess, final ClientApplication clientApplication) throws OauthValidationException;

}
