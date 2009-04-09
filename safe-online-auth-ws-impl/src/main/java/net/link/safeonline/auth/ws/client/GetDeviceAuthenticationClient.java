/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.ws.client;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.lin_k.safe_online.auth.DeviceAuthenticationService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.MessageAccessor;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;


/**
 * Interface for get device authentication client.
 * 
 * @author wvdhaute
 * 
 */
public interface GetDeviceAuthenticationClient extends MessageAccessor {

    /**
     * Returns endpoint reference {@link W3CEndpointReference} to the stateful {@link DeviceAuthenticationService}
     */
    W3CEndpointReference getInstance()
            throws RequestDeniedException, WSClientTransportException;

}
