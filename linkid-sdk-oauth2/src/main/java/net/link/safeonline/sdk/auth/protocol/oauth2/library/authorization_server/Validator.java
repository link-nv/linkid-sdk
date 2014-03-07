/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.oauth2.library.authorization_server;

import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientAccessRequest;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.exceptions.OAuthException;
import net.link.safeonline.sdk.auth.protocol.oauth2.library.messages.*;


/**
 * Validate redirection URIs
 * <p/>
 * Date: 22/03/12
 * Time: 17:16
 *
 * @author sgdesmet
 */
public interface Validator {

    public void validate(AuthorizationRequest request, ClientConfiguration configuration)
            throws OAuthException;

    public void validate(AccessTokenRequest request, ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException;

    public void validate(ValidationRequest request, ClientAccessRequest clientAccessRequest, final ClientConfiguration clientConfiguration)
            throws OAuthException;
}
