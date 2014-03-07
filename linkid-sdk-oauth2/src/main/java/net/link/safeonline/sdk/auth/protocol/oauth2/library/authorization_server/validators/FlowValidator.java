package net.link.safeonline.sdk.auth.protocol.oauth2.library.authorization_server.validators;

import static net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message.*;

import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.AccessTokenRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.AuthorizationRequest;


/**
 * Checks if the oauth flow is allowed by the configured client application
 * <p/>
 * Date: 23/03/12
 * Time: 15:31
 *
 * @author sgdesmet
 */
public class FlowValidator extends AbstractValidator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientConfiguration configuration)
            throws OAuthException {

        requiredClientApplication( configuration, ErrorType.INVALID_CLIENT );
        switch (request.getResponseType()) {
            case CODE:
                if (!configuration.getAllowedFlows().contains( ClientConfiguration.FlowType.AUTHORIZATION ))
                    throw new OAuthException( ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case TOKEN:
                if (!configuration.getAllowedFlows().contains( ClientConfiguration.FlowType.IMPLICIT ))
                    throw new OAuthException( ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
        }
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException {

        switch (request.getGrantType()) {
            case AUTHORIZATION_CODE:
                if (!clientConfiguration.getAllowedFlows().contains( ClientConfiguration.FlowType.AUTHORIZATION ))
                    throw new OAuthException( ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case CLIENT_CREDENTIALS:
                if (!clientConfiguration.getAllowedFlows().contains( ClientConfiguration.FlowType.CLIENT_CREDENTIALS ))
                    throw new OAuthException( ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case PASSWORD:
                if (!clientConfiguration.getAllowedFlows().contains( ClientConfiguration.FlowType.RESOURCE_CREDENTIALS ))
                    throw new OAuthException( ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case REFRESH_TOKEN:
                if (clientAccessRequest == null || clientAccessRequest.getFlowType() == ClientConfiguration.FlowType.CLIENT_CREDENTIALS
                    || clientAccessRequest.getFlowType() == ClientConfiguration.FlowType.IMPLICIT)
                    throw new OAuthException( ErrorType.UNAUTHORIZED_CLIENT, "flow type does not support refresh tokens" );
                break;
        }
    }
}
