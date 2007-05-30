/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SafeOnline Application Console entry point.
 * 
 * @author fcorneli
 * 
 */
public class Main {

	private static final Log LOG = LogFactory.getLog(Main.class);

	public static void main(String[] args) {
		LOG.info("Starting SafeOnline Application Console...");

		new ApplicationConsole();
	}
}
