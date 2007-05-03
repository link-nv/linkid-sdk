/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Parameter filter HTTP request wrapper. This wrapper will filter out a given
 * HTTP request parameter.
 * 
 * @author fcorneli
 * 
 */
public class ParameterFilterHttpServletRequestWrapper extends
		HttpServletRequestWrapper {

	private final String parameterToFilter;

	public ParameterFilterHttpServletRequestWrapper(HttpServletRequest request,
			String parameterToFilter) {
		super(request);
		this.parameterToFilter = parameterToFilter;
	}

	@Override
	public String getParameter(String name) {
		if (this.parameterToFilter.equals(name)) {
			return null;
		}
		return super.getParameter(name);
	}
}
