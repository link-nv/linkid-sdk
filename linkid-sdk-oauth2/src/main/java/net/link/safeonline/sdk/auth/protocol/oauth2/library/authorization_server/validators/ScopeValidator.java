package net.link.safeonline.sdk.auth.protocol.oauth2.library.authorization_server.validators;

import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.AccessTokenRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.AuthorizationRequest;


/**
 * Validate the scope of a request against the preconfigured allowed scope. If no scope is configured,
 * it is interpreted as "deny all"
 * <p/>
 * Date: 23/03/12
 * Time: 15:31
 *
 * @author sgdesmet
 */
public class ScopeValidator extends AbstractValidator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientConfiguration configuration)
            throws OAuthException {

        List<String> requestedScope = request.getScope() != null? request.getScope(): new LinkedList<String>();
        List<String> configuredScope = configuration.getConfiguredScope() != null? configuration.getConfiguredScope(): new LinkedList<String>();
        validate( requestedScope, configuredScope );
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

        List<String> requestedScope = request.getScope() != null? request.getScope(): new LinkedList<String>();
        List<String> configuredScope = clientConfiguration.getConfiguredScope() != null? clientConfiguration.getConfiguredScope(): new LinkedList<String>();
        validate( requestedScope, configuredScope );
    }

    private void validate(List<String> requestedScope, List<String> configuredScope)
            throws OAuthException {

        for (String attribute : requestedScope) {
            if (!configuredScope.contains( attribute ))
                throw new OAuthException( OAuth2Message.ErrorType.INVALID_SCOPE, "Requested scope extends pre-configured scope: " + attribute );
        }
    }
}
