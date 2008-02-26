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
 * Servlet that receives the java version data from the JavaVersionApplet applet
 * and processes it.
 * 
 * @author fcorneli
 * 
 */
public class JavaVersionServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(JavaVersionServlet.class);

	@RequestParameter("appName")
	private String appName;

	@RequestParameter("appVersion")
	private String appVersion;

	@RequestParameter("appMinorVersion")
	private String appMinorVersion;

	@RequestParameter("appCodeName")
	private String appCodeName;

	@RequestParameter("platform")
	private String platformRequestParameter;

	@RequestParameter("userAgent")
	private String userAgent;

	@RequestParameter("vendor")
	private String vendor;

	@RequestParameter("cpuClass")
	private String cpuClass;

	@RequestParameter("javaEnabled")
	private String javaEnabled;

	@RequestParameter("javaVersion")
	private String javaVersion;

	@RequestParameter("javaVendor")
	private String javaVendor;

	@SuppressWarnings("unused")
	@Out(value = "platform", scope = ScopeType.SESSION)
	private PLATFORM platform;

	public static enum PLATFORM {
		WINDOWS, LINUX, MAC
	}

	@Override
	protected void invoke(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		LOG.debug("platform: " + this.platformRequestParameter);
		LOG.debug("java enabled: " + this.javaEnabled);
		LOG.debug("java version: " + this.javaVersion);
		LOG.debug("java vendor: " + this.javaVendor);
		LOG.debug("cpu class: " + this.cpuClass);
		LOG.debug("user agent: " + this.userAgent);
		LOG.debug("vendor: " + this.vendor);
		LOG.debug("app name: " + this.appName);
		LOG.debug("app version: " + this.appVersion);
		LOG.debug("app minor version: " + this.appMinorVersion);
		LOG.debug("app code name: " + this.appCodeName);
		if (false == checkPlatform(response)) {
			return;
		}
		response.sendRedirect("./beid-applet.seam");
	}

	private boolean checkPlatform(HttpServletResponse response)
			throws IOException, ServletException {
		if (null == this.platformRequestParameter) {
			throw new ServletException("platform request parameter required");
		}
		String platformStr = this.platformRequestParameter.toLowerCase();
		if (platformStr.indexOf("win") != -1) {
			this.platform = PLATFORM.WINDOWS;
		} else if (platformStr.indexOf("linux") != -1) {
			this.platform = PLATFORM.LINUX;
		} else if (platformStr.indexOf("mac") != -1) {
			this.platform = PLATFORM.MAC;
		} else {
			response.sendRedirect("./unsupported-platform.seam");
			return false;
		}
		return true;
	}
}
