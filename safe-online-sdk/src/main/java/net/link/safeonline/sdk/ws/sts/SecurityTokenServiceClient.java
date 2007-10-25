/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.sts;

import net.link.safeonline.sdk.ws.MessageAccessor;

/**
 * Interface for Security Token Service WS-Trust client.
 * 
 * @author fcorneli
 */
public interface SecurityTokenServiceClient extends MessageAccessor {
	void invoke();
}
