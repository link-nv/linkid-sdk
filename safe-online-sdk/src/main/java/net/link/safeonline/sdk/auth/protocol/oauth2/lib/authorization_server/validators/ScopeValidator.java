package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccess;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientApplication;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthValidationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.AccessTokenRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.AuthorizationRequest;


/**
 * Validate the scope of a request against the preconfigured allowed scope. If no scope is configured,
 * it is interpreted as "deny all"
 * <p/>
 * Date: 23/03/12
 * Time: 15:31
 *
 * @author: sgdesmet
 */
public class ScopeValidator extends AbstractValidator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientApplication application)
            throws OauthValidationException {

        List<String> requestedScope = request.getScope() != null? request.getScope(): new LinkedList<String>();
        List<String> configuredScope =
                application.getConfiguredScope() != null? application.getConfiguredScope(): new LinkedList<String>();
        validate( requestedScope, configuredScope );

    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

        List<String> requestedScope = request.getScope() != null? request.getScope(): new LinkedList<String>();
        List<String> configuredScope =
                clientApplication.getConfiguredScope() != null? clientApplication.getConfiguredScope(): new LinkedList<String>();
        validate( requestedScope, configuredScope );
    }

    private void validate (List<String> requestedScope, List<String> configuredScope)
            throws OauthValidationException {
        for (String attribute : requestedScope) {
            if (!configuredScope.contains( attribute ))
                throw new OauthValidationException( OAuth2Message.ErrorType.INVALID_SCOPE,
                        "Requested scope extends pre-configured scope: " + attribute );
        }
    }
}
