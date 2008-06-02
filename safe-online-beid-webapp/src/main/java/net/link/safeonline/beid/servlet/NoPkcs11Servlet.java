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

import net.link.safeonline.beid.servlet.JavaVersionServlet.JAVA_VERSION;
import net.link.safeonline.servlet.AbstractInjectionServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;

/**
 * Servlet that makes a decision in case the client-side applet did not detect a
 * PKCS#11 library.
 * 
 * @author fcorneli
 * 
 */
public class NoPkcs11Servlet extends AbstractInjectionServlet {

	private static final Log LOG = LogFactory.getLog(NoPkcs11Servlet.class);

	private static final long serialVersionUID = 1L;

	@In(value = JavaVersionServlet.JAVA_VERSION_NAME, scope = ScopeType.SESSION, required = true)
	private JAVA_VERSION javaVersion;

	@Override
	protected void invoke(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("java version: " + this.javaVersion);
		if (this.javaVersion == JAVA_VERSION.JAVA_1_5) {
			response.sendRedirect("./missing-middleware.seam");
		} else {
			response.sendRedirect("./beid-pcsc.seam");
		}
	}
}
