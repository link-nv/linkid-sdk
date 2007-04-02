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
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;

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
		return setUp(servletClass, null, null);
	}

	private static class LocalHashSessionManager extends HashSessionManager {

		private static final Log LOG = LogFactory
				.getLog(LocalHashSessionManager.class);

		private final Map<String, String> initialSessionAttributes;

		public LocalHashSessionManager(
				Map<String, String> initialSessionAttributes) {
			this.initialSessionAttributes = initialSessionAttributes;
		}

		@Override
		protected Session newSession(HttpServletRequest request) {
			LOG.debug("newSession");
			Session session = (Session) super.newSession(request);
			if (null != this.initialSessionAttributes) {
				for (Map.Entry<String, String> entry : this.initialSessionAttributes
						.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					session.setAttribute(key, value);
				}
			}
			return session;
		}
	}

	public String setUp(Class<?> servletClass, Class<?> filterClass,
			Map<String, String> initialSessionAttributes) throws Exception {
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
			context.addFilter(filterClass, "/", Handler.DEFAULT);
		}

		ServletHandler handler = context.getServletHandler();

		ServletHolder servletHolder = new ServletHolder();
		servletHolder.setClassName(servletClass.getName());
		String servletName = "TestServlet";
		servletHolder.setName(servletName);
		handler.addServlet(servletHolder);

		ServletMapping servletMapping = new ServletMapping();
		servletMapping.setServletName(servletName);
		servletMapping.setPathSpecs(new String[] { "/*" });
		handler.addServletMapping(servletMapping);

		this.server.start();

		int port = connector.getLocalPort();
		LOG.debug("port: " + port);

		this.servletLocation = "http://localhost:" + port + "/";
		return this.servletLocation;
	}

	public String getServletLocation() {
		return this.servletLocation;
	}

	public void tearDown() throws Exception {
		this.server.stop();
	}
}
