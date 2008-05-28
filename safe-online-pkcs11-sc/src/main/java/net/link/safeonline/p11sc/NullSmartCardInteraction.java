/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.util.Locale;

/**
 * /dev/null implementation of the smart card interaction interface.
 * 
 * @author fcorneli
 * 
 */
public class NullSmartCardInteraction implements SmartCardInteraction {

	public void output(String message) {
		// empty
	}

	public Locale getLocale() {
		Locale locale = Locale.getDefault();
		return locale;
	}
}
