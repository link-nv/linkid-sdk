/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.version;

import java.applet.Applet;

public class JavaVersionApplet extends Applet {

	private static final long serialVersionUID = 1L;

	private final String version;

	private final String vendor;

	public JavaVersionApplet() {
		this.version = System.getProperty("java.version");
		this.vendor = System.getProperty("java.vendor");
	}

	public String getVersion() {
		return this.version;
	}

	public String getVendor() {
		return this.vendor;
	}
}
