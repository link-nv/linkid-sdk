/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp.filter;

/**
 * 
 * 
 * @author lhunath
 */
public enum ProfileStats {

	REQUEST_TIME("X-Profiler-Request-Time",
			"The time it took to process the request and build a response.");

	private String description;
	private String header;

	private ProfileStats(String header, String description) {

		this.header = header;
		this.description = description;
	}

	public String getDescription() {

		return description;
	}

	public String getHeader() {

		return header;
	}
}
