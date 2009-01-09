/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.appconsole;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.AuthenticationProtocol;
import net.link.safeonline.sdk.auth.filter.AuthnRequestFilter;
import net.link.safeonline.sdk.auth.filter.AuthnResponseFilter;
import net.link.safeonline.sdk.auth.filter.LogFilter;
import net.link.safeonline.sdk.auth.filter.LoginManager;

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

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;


/**
 * Jetty servlet to load the application authentication page
 * 
 * @author wvdhaute
 * 
 */
public class AuthServletManager extends Observable {

    static final Log                  LOG = LogFactory.getLog(AuthServletManager.class);

    private static AuthServletManager authServletManager;

    private Server                    server;

    private Connector                 connector;

    private Context                   context;


    public static AuthServletManager getInstance() {

        if (null == authServletManager) {
            authServletManager = new AuthServletManager();
        }
        return authServletManager;
    }

    private AuthServletManager() {

    }

    public void authenticate(String applicationName, String protocol)
            throws Exception {

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

        context.addFilter(AuthnResponseFilter.class, "/", Handler.DEFAULT);

        FilterHolder authenticationFilterHoldder = context.addFilter(AuthnRequestFilter.class, "/", Handler.DEFAULT);
        Map<String, String> filterInitParameters = new HashMap<String, String>();
        filterInitParameters.put("AuthenticationServiceUrl", "http://" + ApplicationConsoleManager.getInstance().getLocation()
                + ":8080/olas-auth");
        filterInitParameters.put("ApplicationName", applicationName);
        filterInitParameters.put("AuthenticationProtocol", protocol);
        if (protocol.equals(AuthenticationProtocol.SAML2_BROWSER_POST.toString())) {
            filterInitParameters.put("KeyStoreFile", ApplicationConsoleManager.getInstance().getKeyStorePath());
            filterInitParameters.put("KeyStoreType", ApplicationConsoleManager.getInstance().getKeyStoreType());
            filterInitParameters.put("KeyStorePassword", ApplicationConsoleManager.getInstance().getKeyStorePassword());
        }

        authenticationFilterHoldder.setInitParameters(filterInitParameters);

        ServletHandler handler = context.getServletHandler();

        ServletHolder servletHolder = new ServletHolder();
        String servletClassName = ApplicationServlet.class.getName();
        servletHolder.setClassName(servletClassName);
        String servletName = "AuthServlet";
        servletHolder.setName(servletName);
        handler.addServlet(servletHolder);

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName(servletName);
        servletMapping.setPathSpecs(new String[] { "/*" });
        handler.addServletMapping(servletMapping);

        server.start();

        int port = connector.getLocalPort();

        String servletLocation = "http://localhost:" + port + "/";

        BrowserLauncher browserLauncher;
        try {
            browserLauncher = new BrowserLauncher();
            browserLauncher.openURLinBrowser(servletLocation);
        } catch (BrowserLaunchingInitializingException e) {
            LOG.error("BrowserLaunchingInitializingException thrown ...", e);
        } catch (UnsupportedOperatingSystemException e) {
            LOG.error("UnsupportedOperatingSystemException thrown ...", e);
        }

    }


    public static class ApplicationServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;


        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {

            LOG.debug("doGet");
            HttpSession session = req.getSession();
            String userId = (String) session.getAttribute(LoginManager.USERID_SESSION_ATTRIBUTE);
            LOG.debug("userId: " + userId);
            resp.getWriter().println("Success authenticating user " + userId + ".\nYou may close this browser now...");
            resp.flushBuffer();
            if (null != userId) {
                AuthServletManager.getInstance().shutDownJetty();
            }
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws IOException {

            doGet(request, response);
        }
    }


    public void shutDownJetty() {

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
