package net.link.safeonline.sdk.auth.protocol.oauth2.lib.authorization_server.validators;

import static net.link.safeonline.sdk.auth.protocol.oauth2.lib.OAuth2Message.*;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientAccess;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientApplication;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.OauthValidationException;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.AccessTokenRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.messages.AuthorizationRequest;


/**
 * Checks if the oauth flow is allowed by the configured client application
 * <p/>
 * Date: 23/03/12
 * Time: 15:31
 *
 * @author: sgdesmet
 */
public class FlowValidator extends AbstractValidator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientApplication application)
            throws OauthValidationException {

        requiredClientApplication( application, ErrorType.INVALID_CLIENT );
        switch (request.getResponseType()) {
            case CODE:
                if (!application.getAllowedFlows().contains( ClientApplication.FlowType.AUTHORIZATION ))
                    throw new OauthValidationException(ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case TOKEN:
                if (!application.getAllowedFlows().contains( ClientApplication.FlowType.IMPLICIT ))
                    throw new OauthValidationException(ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
        }
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccess clientAccess, final ClientApplication clientApplication)
            throws OauthValidationException {

        switch ( request.getGrantType() ){
            case AUTHORIZATION_CODE:
                if (!clientApplication.getAllowedFlows().contains( ClientApplication.FlowType.AUTHORIZATION ))
                    throw new OauthValidationException(ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case CLIENT_CREDENTIALS:
                if (!clientApplication.getAllowedFlows().contains( ClientApplication.FlowType.CLIENT_CREDENTIALS ))
                    throw new OauthValidationException(ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case PASSWORD:
                if (!clientApplication.getAllowedFlows().contains( ClientApplication.FlowType.RESOURCE_CREDENTIALS ))
                    throw new OauthValidationException(ErrorType.UNAUTHORIZED_CLIENT, "invalid flow type for client" );
                break;
            case REFRESH_TOKEN:
                if (clientAccess == null || clientAccess.getFlowType() == ClientApplication.FlowType.CLIENT_CREDENTIALS
                    || clientAccess.getFlowType() == ClientApplication.FlowType.IMPLICIT)
                    throw new OauthValidationException( ErrorType.UNAUTHORIZED_CLIENT, "flow type does not support refresh tokens" );
                break;
        }
    }
}
