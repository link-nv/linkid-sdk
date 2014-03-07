/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.authorization_server.validators;

import net.link.safeonline.sdk.auth.protocol.oauth2.library.OAuth2Message;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.*;


/**
 * Checks if the client is properly configured
 * <p/>
 * Date: 11/05/12
 * Time: 11:14
 *
 * @author sgdesmet
 */
public class ClientValidator extends AbstractValidator {

    @Override
    public void validate(final AuthorizationRequest request, final ClientConfiguration configuration)
            throws OAuthException {

        checkConfiguration( configuration );
    }

    @Override
    public void validate(final AccessTokenRequest request, final ClientAccessRequest clientAccessRequest,
                         final ClientConfiguration clientConfiguration)
            throws OAuthException {

        checkConfiguration( clientConfiguration );
        checkCorrectMatch( clientAccessRequest, clientConfiguration );
    }

    @Override
    public void validate(final ValidationRequest request, final ClientAccessRequest clientAccessRequest,
                         final ClientConfiguration clientConfiguration)
            throws OAuthException {

        checkConfiguration( clientConfiguration );
        checkCorrectMatch( clientAccessRequest, clientConfiguration );
    }

    protected void checkConfiguration(ClientConfiguration configuration)
            throws OAuthException {

        if (configuration != null) {
            if (configuration.isConfidential() && MessageUtils.stringEmpty( configuration.getClientSecret() )) {
                throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR,
                        "client " + configuration.getClientId() + "is not properly configured" );
            }
            if (!configuration.isConfidential() && MessageUtils.collectionEmpty( configuration.getRedirectUris() )) {
                throw new OAuthException( OAuth2Message.ErrorType.SERVER_ERROR,
                        "client " + configuration.getClientId() + "is not properly configured" );
            }
        }
    }

    protected void checkCorrectMatch(ClientAccessRequest accessRequest, ClientConfiguration clientConfiguration)
            throws OAuthException {

        if (accessRequest != null && clientConfiguration != null && !clientConfiguration.getClientId()
                                                                                        .equals(
                                                                                                accessRequest.getClient().getClientId() )) {
            throw new OAuthException( OAuth2Message.ErrorType.INVALID_REQUEST, "token is not meant for this client" );
        }
    }
}
