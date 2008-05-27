/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.io.IOException;

/**
 * Signals that no smart card reader is present on the system.
 * 
 * @author fcorneli
 * 
 */
public class MissingSmartCardReaderException extends IOException {

	private static final long serialVersionUID = 1L;

	public MissingSmartCardReaderException() {
		super();
	}

	public MissingSmartCardReaderException(String s) {
		super(s);
	}
}
