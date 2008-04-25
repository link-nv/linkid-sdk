/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.RequestParameter;

/**
 * Servlet that processes the data that comes from the client-side
 * identification applet.
 * 
 * @author fcorneli
 * 
 */
public class IdentificationDataServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(IdentificationDataServlet.class);

	public static final String NAME_SESSION_ATTRIBUTE = "id-name";
	
	@RequestParameter("name")
	@Out(value = NAME_SESSION_ATTRIBUTE, scope = ScopeType.SESSION)
	private String name;

	@RequestParameter("firstname")
	@Out(value = "id-firstname", scope = ScopeType.SESSION)
	private String firstName;

	@RequestParameter("dob")
	private String dob;

	@RequestParameter("nationality")
	@Out(value = "id-nationality", scope = ScopeType.SESSION)
	private String nationality;

	@RequestParameter("sex")
	@Out(value = "id-sex", scope = ScopeType.SESSION)
	private String sex;

	@RequestParameter("street")
	private String street;

	@RequestParameter("city")
	@Out(value = "id-city", scope = ScopeType.SESSION)
	private String city;

	@RequestParameter("zip")
	@Out(value = "id-zip", scope = ScopeType.SESSION)
	private String zip;

	@Override
	protected void invoke(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("name: " + this.name);
		LOG.debug("first name: " + this.firstName);
		LOG.debug("dob: " + this.dob);
		LOG.debug("nationality: " + this.nationality);
		LOG.debug("sex: " + this.sex);
		LOG.debug("street: " + this.street);
		LOG.debug("city: " + this.city);
		LOG.debug("zip: " + this.zip);
	}
}
