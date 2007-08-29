/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.servlet.AbstractSessionManager.Session;

/**
 * Servlet Test Manager. This test manager allows one to unit test servlets. It
 * is using the embeddable Jetty servlet container.
 * 
 * @author fcorneli
 * 
 */
public class ServletTestManager {

	private static final Log LOG = LogFactory.getLog(ServletTestManager.class);

	private Server server;

	private String servletLocation;

	public String setUp(Class<?> servletClass) throws Exception {
		return setUp(servletClass, null, null, null);
	}

	public String setUp(Class<?> servletClass,
			Map<String, String> servletInitParameters) throws Exception {
		return setUp(servletClass, servletInitParameters, null, null, null);
	}

	private Session session;

	private class LocalHashSessionManager extends HashSessionManager {

		private final Log LOG = LogFactory
				.getLog(LocalHashSessionManager.class);

		private final Map<String, Object> initialSessionAttributes;

		public LocalHashSessionManager(
				Map<String, Object> initialSessionAttributes) {
			this.initialSessionAttributes = initialSessionAttributes;
		}

		@Override
		protected Session newSession(HttpServletRequest request) {
			LOG.debug("newSession");
			Session session = (Session) super.newSession(request);
			if (null != this.initialSessionAttributes) {
				for (Map.Entry<String, Object> entry : this.initialSessionAttributes
						.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					session.setAttribute(key, value);
				}
			}
			ServletTestManager.this.session = session;
			return session;
		}
	}

	public String setUp(Class<?> servletClass, Class<?> filterClass,
			Map<String, String> filterInitParameters,
			Map<String, Object> initialSessionAttributes) throws Exception {
		return setUp(servletClass, null, filterClass, filterInitParameters,
				initialSessionAttributes);
	}

	public String setUp(Class<?> servletClass,
			Map<String, String> servletInitParameters, Class<?> filterClass,
			Map<String, String> filterInitParameters,
			Map<String, Object> initialSessionAttributes) throws Exception {
		this.server = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(0);
		this.server.addConnector(connector);

		SessionManager sessionManager = new LocalHashSessionManager(
				initialSessionAttributes);
		SessionHandler sessionHandler = new SessionHandler(sessionManager);

		Context context = new Context(null, sessionHandler, null, null, null);
		context.setContextPath("/");
		this.server.addHandler(context);

		if (null != filterClass) {
			FilterHolder filterHolder = context.addFilter(filterClass, "/",
					Handler.DEFAULT);
			if (null != filterInitParameters) {
				filterHolder.setInitParameters(filterInitParameters);
			}
		}

		ServletHandler handler = context.getServletHandler();

		ServletHolder servletHolder = new ServletHolder();
		String servletClassName = servletClass.getName();
		servletHolder.setClassName(servletClassName);
		String servletName = "TestServlet";
		servletHolder.setName(servletName);
		if (null != servletInitParameters) {
			servletHolder.setInitParameters(servletInitParameters);
		}
		handler.addServlet(servletHolder);

		ServletMapping servletMapping = new ServletMapping();
		servletMapping.setServletName(servletName);
		servletMapping.setPathSpecs(new String[] { "/*" });
		handler.addServletMapping(servletMapping);

		this.server.start();

		int port = connector.getLocalPort();
		LOG.debug("servlet \"" + servletClass.getSimpleName() + "\" on port "
				+ port);

		this.servletLocation = "http://localhost:" + port + "/";
		return this.servletLocation;
	}

	public String getServletLocation() {
		return this.servletLocation;
	}

	public void tearDown() throws Exception {
		this.server.stop();
	}

	public Object getSessionAttribute(String name) {
		if (null == this.session) {
			return null;
		}
		Object attribute = this.session.getAttribute(name);
		return attribute;
	}

	public void setSessionAttribute(String name, Object value) {
		if (null == this.session) {
			return;
		}

	}
}
