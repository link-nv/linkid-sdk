/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth.client;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import net.link.safeonline.sdk.api.exception.RequestDeniedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;


/**
 * Interface for get authentication client.
 *
 * @author wvdhaute
 */
public interface GetAuthenticationClient {

    /**
     * Returns endpoint reference {@link W3CEndpointReference} to the stateful WS Authentication service.
     *
     * @return EPR
     *
     * @throws RequestDeniedException     request was denied
     * @throws WSClientTransportException WS request transport failed somewhere
     */
    W3CEndpointReference getInstance()
            throws RequestDeniedException, WSClientTransportException;
}
