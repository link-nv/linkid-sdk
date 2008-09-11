/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.option.device.exception;

/**
 * <h2>{@link ConnectionException}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 10, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class ConnectionException extends OptionDeviceException {

	private static final long serialVersionUID = 1L;

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
