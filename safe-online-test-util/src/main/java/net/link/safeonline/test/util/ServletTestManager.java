/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.LocalConnector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;


/**
 * Servlet Test Manager. This test manager allows one to unit test servlets. It is using the embeddable Jetty servlet
 * container.
 * 
 * @author fcorneli
 * 
 */
public class ServletTestManager {

    static final Log LOG = LogFactory.getLog(ServletTestManager.class);

    private Server   server;

    private String   contextPath;


    public static class TestHashSessionManager extends HashSessionManager {

        final Map<String, Object> initialSessionAttributes;


        public TestHashSessionManager() {

            this.initialSessionAttributes = new HashMap<String, Object>();
        }

        public void setInitialSessionAttribute(String name, Object value) {

            this.initialSessionAttributes.put(name, value);
        }

        @Override
        protected Session newSession(HttpServletRequest request) {

            LOG.debug("new session");
            Session session = (Session) super.newSession(request);
            for (Map.Entry<String, Object> mapEntry : this.initialSessionAttributes.entrySet()) {
                LOG.debug("setting attribute: " + mapEntry.getKey());
                session.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
            return session;
        }
    }


    public void setUp(Class<?> servletClass) throws Exception {

        setUp(servletClass, null, null, null);
    }

    public void setUp(Class<?> servletClass, Map<String, String> servletInitParameters) throws Exception {

        setUp(servletClass, servletInitParameters, null, null, null);
    }


    TestHashSessionManager sessionManager;


    public void setUp(Class<?> servletClass, Class<?> filterClass) throws Exception {

        setUp(servletClass, null, filterClass, null, null);
    }

    public void setUp(Class<?> servletClass, Class<?> filterClass, Map<String, String> filterInitParameters)
            throws Exception {

        setUp(servletClass, null, filterClass, filterInitParameters, null);
    }

    public void setUp(Class<?> servletClass, Class<?> filterClass, Map<String, String> filterInitParameters,
            Map<String, Object> initialSessionAttributes) throws Exception {

        setUp(servletClass, null, filterClass, filterInitParameters, initialSessionAttributes);
    }

    public void setUp(Class<?> servletClass, Map<String, String> servletInitParameters, Class<?> filterClass,
            Map<String, String> filterInitParameters, Map<String, Object> initialSessionAttributes) throws Exception {

        setUp(servletClass, "/", servletInitParameters, filterClass, filterInitParameters, initialSessionAttributes);
    }

    public void setUp(Class<?> servletClass, String contextPath, Map<String, String> servletInitParameters,
            Class<?> filterClass, Map<String, String> filterInitParameters, Map<String, Object> initialSessionAttributes)
            throws Exception {

        this.server = new Server();
        this.contextPath = contextPath;

        Connector connector = new LocalConnector();
        this.sessionManager = new TestHashSessionManager();
        Context context = new Context(null, new SessionHandler(this.sessionManager), new SecurityHandler(), null, null);
        context.setContextPath(contextPath);
        this.server.addConnector(connector);
        this.server.addHandler(context);

        if (null != servletInitParameters) {
            context.setInitParams(servletInitParameters);
        }

        if (null != filterClass) {
            FilterHolder filterHolder = context.addFilter(filterClass, this.contextPath, Handler.DEFAULT);
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
        servletMapping.setPathSpecs(new String[] { "/*", this.contextPath });
        handler.addServletMapping(servletMapping);

        this.server.start();

        if (null != initialSessionAttributes) {
            this.sessionManager.initialSessionAttributes.putAll(initialSessionAttributes);
        }
    }

    private String createSocketConnector() throws Exception {

        SocketConnector connector = new SocketConnector();
        connector.setHost("127.0.0.1");
        this.server.addConnector(connector);
        if (this.server.isStarted()) {
            connector.start();
        } else {
            connector.open();
        }

        return "http://127.0.0.1:" + connector.getLocalPort();
    }

    public String getServletLocation() throws Exception {

        return createSocketConnector() + this.contextPath;
    }

    public void tearDown() throws Exception {

        this.server.stop();
    }

    @SuppressWarnings( { "unchecked" })
    public Object getSessionAttribute(String name) {

        Map<String, AbstractSessionManager.Session> sessions = this.sessionManager.getSessionMap();
        AbstractSessionManager.Session session = sessions.values().iterator().next();
        String sessionId = session.getId();
        LOG.debug("session id: " + sessionId);
        Object value = session.getAttribute(name);
        return value;
    }

    /**
     * We update all existing sessions + we make sure that new session also get this session attribute.
     * 
     * @param name
     * @param value
     */
    @SuppressWarnings( { "unchecked" })
    public void setSessionAttribute(String name, Object value) {

        Map<String, AbstractSessionManager.Session> sessions = this.sessionManager.getSessionMap();
        for (AbstractSessionManager.Session session : sessions.values()) {
            session.setAttribute(name, value);
        }
        this.sessionManager.setInitialSessionAttribute(name, value);
    }
}
