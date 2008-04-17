/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

public class NullPcscSignerLogger implements PcscSignerLogger {

	@Override
	public void log(String message) {
		// empty
	}
}
