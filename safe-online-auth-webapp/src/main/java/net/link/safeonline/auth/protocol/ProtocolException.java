/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

/**
 * Thrown when a protocol handler detects a violation against the corresponding
 * authentication protocol. The protocol error message will be displayed in the
 * protocol error message result page.
 * 
 * @author fcorneli
 * 
 */
public class ProtocolException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Main constructor.
	 * 
	 * @param message
	 *            the protocol error message.
	 */
	public ProtocolException(String message) {
		super(message);
	}
}
