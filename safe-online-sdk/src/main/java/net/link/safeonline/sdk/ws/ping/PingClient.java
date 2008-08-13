/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.ping;

import java.net.ConnectException;

import net.link.safeonline.sdk.ws.MessageAccessor;


/**
 * Interface for ping client. The ping web service can be used to quickly check for the availability of the SafeOnline
 * services.
 * 
 * @author fcorneli
 * 
 */
public interface PingClient extends MessageAccessor {

    void ping() throws ConnectException;
}
