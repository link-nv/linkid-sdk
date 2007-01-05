/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import net.link.safeonline.p11sc.impl.SmartCardImpl;

public class SmartCardFactory {

	private SmartCardFactory() {
		// empty
	}

	public static SmartCard newInstance() {
		SmartCard instance = new SmartCardImpl();
		return instance;
	}
}
