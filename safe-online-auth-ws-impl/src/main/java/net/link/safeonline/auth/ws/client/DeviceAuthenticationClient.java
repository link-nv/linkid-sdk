/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws.client;

import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;


/**
 * Interface for device authentication client. Via components implementing this interface applications can authenticate against OLAS. *
 * 
 * @author wvdhaute
 * 
 */
public interface DeviceAuthenticationClient extends MessageAccessor {

    /**
     * Proxies authentication request
     * 
     * @throws WSClientTransportException
     *             in case the service could not be contacted. Can happen if the SSL was not setup correctly.
     * @throws RequestDeniedException
     * @throws WSAuthenticationException
     */
    WSAuthenticationResponseType authenticate(WSAuthenticationRequestType request)
            throws WSClientTransportException, RequestDeniedException, WSAuthenticationException;
}
