/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth.client;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;


/**
 * Factory to return statful {@link AuthenticationClient} instances.
 *
 * @author wvdhaute
 */
public interface AuthenticationClientFactory {

    /**
     * @return endpoint reference {@link W3CEndpointReference} to the stateful WS Authentication service.
     *
     * @throws WSClientTransportException WS request transport failed somewhere
     */
    W3CEndpointReference getInstance()
            throws WSClientTransportException;
}
