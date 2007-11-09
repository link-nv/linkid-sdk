/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.encap.activation;

import java.rmi.RemoteException;

public interface EncapActivationClient {

	String activate(String mobile, String orgId, String userId)
			throws RemoteException;

	boolean cancelSession(String sessionId) throws RemoteException;

}
