/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console.device;

import java.util.Observable;

import net.link.safeonline.sdk.auth.filter.LogFilter;

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
 * Jetty servlet to load the application authentication page
 * 
 * @author wvdhaute
 * 
 */
public class BeIdAuthenticationServletManager extends Observable {

    static final Log                                LOG = LogFactory.getLog(BeIdAuthenticationServletManager.class);

    private static BeIdAuthenticationServletManager beidAuthServletManager;

    private Server                                  server;

    private Connector                               connector;

    private Context                                 context;


    public static BeIdAuthenticationServletManager getInstance() {

        if (null == beidAuthServletManager) {
            beidAuthServletManager = new BeIdAuthenticationServletManager();
        }
        return beidAuthServletManager;
    }

    private BeIdAuthenticationServletManager() {

    }

    public String start()
            throws Exception {

        LOG.info("Starting Jetty ......");

        server = new Server();
        connector = new SelectChannelConnector();
        connector.setPort(0);
        server.addConnector(connector);

        SessionManager sessionManager = new HashSessionManager();
        SessionHandler sessionHandler = new SessionHandler(sessionManager);

        context = new Context(null, sessionHandler, null, null, null);
        context.setContextPath("/");
        server.addHandler(context);

        context.addFilter(LogFilter.class, "/", Handler.DEFAULT);

        ServletHandler handler = context.getServletHandler();

        ServletHolder servletHolder = new ServletHolder();
        String servletClassName = BeIdAuthenticationServlet.class.getName();
        servletHolder.setClassName(servletClassName);
        String servletName = "BeIdAuthenticationServlet";
        servletHolder.setName(servletName);
        handler.addServlet(servletHolder);

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName(servletName);
        servletMapping.setPathSpecs(new String[] { "/*" });
        handler.addServletMapping(servletMapping);

        server.start();

        int port = connector.getLocalPort();
        return "http://localhost:" + port + "/";

    }

    public void shutDown() {

        LOG.info("Shutting down Jetty ......");
        try {
            connector.stop();
        } catch (Exception e) {
            LOG.error("Failed to shutdown Jetty", e);
        }
        context.setShutdown(true);
        setChanged();
        notifyObservers(Boolean.TRUE);
    }
}
