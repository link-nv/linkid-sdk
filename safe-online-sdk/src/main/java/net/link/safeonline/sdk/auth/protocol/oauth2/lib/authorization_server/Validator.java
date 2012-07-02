package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OAuthException;
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

    public void validate(AuthorizationRequest request, ClientConfiguration configuration) throws OAuthException;

    public void validate(AccessTokenRequest request, ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration) throws OAuthException;

    public void validate(ValidationRequest request, ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration) throws OAuthException;

}
