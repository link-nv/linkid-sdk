/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.filter;

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;

/**
 * <h2>{@link ProfiledException} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Nov 30, 2007</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ProfiledException extends ServletException {

	private static final long serialVersionUID = 1L;
	private Map<String, String> headers;

	/**
	 * Create a new {@link ProfiledException} instance.
	 */
	public ProfiledException(Exception e, Map<String, String> headers) {

		super(e);

		this.headers = headers;
	}

	/**
	 * @return The headers of this {@link ProfiledException}.
	 */
	public Map<String, String> getHeaders() {

		return Collections.unmodifiableMap(this.headers);
	}
}
