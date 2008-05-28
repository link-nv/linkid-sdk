/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.io.IOException;

/**
 * Signals that the smart card is not supported for authentication.
 * 
 * @author fcorneli
 * 
 */
public class UnsupportedSmartCardException extends IOException {

	private static final long serialVersionUID = 1L;

	public UnsupportedSmartCardException() {
		super();
	}

	public UnsupportedSmartCardException(String s) {
		super(s);
	}
}
