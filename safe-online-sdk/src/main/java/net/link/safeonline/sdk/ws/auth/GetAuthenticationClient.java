/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.auth;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.AuthenticationService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;


/**
 * Interface for get authentication client.
 * 
 * @author wvdhaute
 * 
 */
public interface GetAuthenticationClient extends MessageAccessor {

    /**
     * Returns EPR to the stateful {@link AuthenticationService}
     * 
     * TODO
     */
    W3CEndpointReference getInstance()
            throws RequestDeniedException, WSClientTransportException;

}
