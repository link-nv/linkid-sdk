/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.util.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link SafeOnlineConfig}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 16, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class SafeOnlineConfig extends Properties {

    public static final String      OLAS_NODE_NAME            = "olas.node.name";
    public static final String      OLAS_HOST_NAME            = "olas.host.name";
    public static final String      OLAS_HOST_PORT            = "olas.host.port";
    public static final String      OLAS_HOST_PORT_SSL        = "olas.host.port.ssl";
    public static final String      OLAS_HOST_PROTOCOL        = "olas.host.protocol";
    public static final String      OLAS_HOST_PROTOCOL_SSL    = "olas.host.protocol.ssl";

    public static final String      APPBASE                   = "appbase";
    public static final String      APPLANDINGBASE            = "applandingbase";

    public static final String      AUTHBASE                  = "authbase";
    public static final String      WSBASE                    = "wsbase";

    private static final long       serialVersionUID          = 1L;
    private static final Log        LOG                       = LogFactory.getLog(SafeOnlineConfig.class);

    private static final String     SAFEONLINE_CONFIG         = "../conf/safeonline.properties";
    private static SafeOnlineConfig config;

    /**
     * The absolute base path for the web application.
     * 
     * <p>
     * <b>NOTE:</b> Put slashes before and after: /myapp/
     * </p>
     */
    public static final String      WEBAPP_PATH_CONTEXT_PARAM = "WebappPath";


    /**
     * Property that defines the name of this OLAS node.
     */
    public static String nodeName() {

        return load().getProperty(OLAS_NODE_NAME);
    }

    /**
     * Property that defines the hostname that clients talk to for reaching our node.
     */
    public static String nodeHost() {

        return load().getProperty(OLAS_HOST_NAME);
    }

    /**
     * Property that defines the port on which the web server accepts connections for {@link #nodeProtocol()}.
     */
    public static int nodePort() {

        return Integer.parseInt(load().getProperty(OLAS_HOST_PORT));
    }

    /**
     * Property that defines the port on which the web server accepts connections for {@link #nodeProtocolSecure()}.
     */
    public static int nodePortSecure() {

        return Integer.parseInt(load().getProperty(OLAS_HOST_PORT_SSL));
    }

    /**
     * Property that defines the web protocol to use general communication.
     * 
     * <ul>
     * <li><code>HTTP</code> <i>[recommended]</i></li>
     * <li><code>HTTPS</code></li>
     * </ul>
     */
    public static String nodeProtocol() {

        return load().getProperty(OLAS_HOST_PROTOCOL);
    }

    /**
     * Property that defines the web protocol to use for communication of private data.
     * 
     * <ul>
     * <li><code>HTTP</code></li>
     * <li><code>HTTPS</code> <i>[recommended]</i></li>
     * </ul>
     */
    public static String nodeProtocolSecure() {

        return load().getProperty(OLAS_HOST_PROTOCOL_SSL);
    }

    /**
     * Property that defines the base URL for applications on this host.
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>http://my.host.be</code>) <i>[required]</i>
     * </p>
     */
    public static String appbase() {

        return load().getProperty(APPBASE);
    }

    /**
     * Property that defines the base URL for application landing pages on this host (should be on HTTPS).
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>https://my.host.be</code>) <i>[required]</i>
     * </p>
     */
    public static String applandingbase() {

        return load().getProperty(APPLANDINGBASE);
    }

    /**
     * Property that defines the base URL to the OLAS authentication web application to use for application authentication.
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]/[path-to-olas-auth]</code> (eg. <code>https://my.olas.be/olas-auth</code>)
     * <i>[required]</i>
     * </p>
     */
    public static String authbase() {

        return load().getProperty(AUTHBASE);
    }

    /**
     * Property that defines the base URL to the OLAS web services to use.
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>https://my.olas.be</code>) <i>[required]</i>
     * </p>
     */
    public static String wsbase() {

        return load().getProperty(WSBASE);
    }

    /**
     * The endpoint URL is the base URL at which the application runs.
     * 
     * <p>
     * <b>Note:</b> This method uses {@link #appbase()}. Whenever an application <b>inside a node</b> needs to know its endpoint, it should
     * <b>use its node's location attribute instead</b>.
     * </p>
     * 
     * @return The endpoint URL for the web application triggered by the given request.
     */
    public static String getApplicationEndpointFor(HttpServletRequest request) {

        return appbase() + webappPath(request);
    }

    /**
     * The landingpoint URL is the base URL at which the application's landing servlets are bound. This is to provide a seamless transition
     * between HTTPS and HTTP.
     * 
     * <p>
     * <b>Note:</b> This method uses {@link #applandingbase()}. Whenever an application <b>inside a node</b> needs to know its endpoint, it
     * should <b>use its node's location attribute instead</b>.
     * </p>
     * 
     * @return The endpoint URL for the web application triggered by the given request.
     */
    public static String getApplicationLandingpointFor(HttpServletRequest request) {

        return applandingbase() + webappPath(request);
    }

    /**
     * The path of the web application that services the given request.
     */
    public static String webappPath(HttpServletRequest request) {

        return getValue(request, WEBAPP_PATH_CONTEXT_PARAM);
    }

    /**
     * <p>
     * <b>Note: Only for use by OLAS applications.</b> Node services should use their node's location to create the absolute URL.
     * </p>
     * 
     * @return An absolute URL within the current web application using the specified path.
     */
    public static String absoluteApplicationUrlFromPath(HttpServletRequest request, String path) {

        if (URI.create(path).isAbsolute())
            return path;

        String appPath = path;
        if (appPath.charAt(0) == '/') {
            appPath = path.substring(1);
        }

        String absolutePath = getApplicationEndpointFor(request) + appPath;
        LOG.debug("Absolute path for '" + path + "' is: " + absolutePath);

        return absolutePath;
    }

    /**
     * <p>
     * <b>Note: Only for use by OLAS applications.</b> Node services should use their node's location to create the absolute URL.
     * </p>
     * 
     * @return An absolute URL within the current web application using the path specified in the given context parameter.
     */
    public static String absoluteApplicationUrlFromParam(HttpServletRequest request, String contextParamName) {

        LOG.debug("Looking up absolute path for " + contextParamName + " ..");
        return absoluteApplicationUrlFromPath(request, request.getSession().getServletContext().getInitParameter(contextParamName));
    }

    /**
     * <p>
     * <b>Note: Only for use by OLAS applications.</b> Node services should use their node's location to create the absolute URL.
     * </p>
     * 
     * @return An absolute URL within the current web application using the specified path.
     */
    public static String absoluteApplicationLandingUrlFromPath(HttpServletRequest request, String path) {

        if (URI.create(path).isAbsolute())
            return path;

        String appPath = path;
        if (appPath.charAt(0) == '/') {
            appPath = path.substring(1);
        }

        String absolutePath = getApplicationLandingpointFor(request) + appPath;
        LOG.debug("Absolute landing path for '" + path + "' is: " + absolutePath);

        return absolutePath;
    }

    /**
     * <p>
     * <b>Note: Only for use by OLAS applications.</b> Node services should use their node's location to create the absolute URL.
     * </p>
     * 
     * @return An absolute URL within the current web application using the path specified in the given context parameter.
     */
    public static String absoluteApplicationLandingUrlFromParam(HttpServletRequest request, String contextParamName) {

        LOG.debug("Looking up absolute landing path for " + contextParamName + " ..");
        return absoluteApplicationLandingUrlFromPath(request, request.getSession().getServletContext().getInitParameter(contextParamName));
    }

    /**
     * Load the web application configuration from the specified properties file if it's not cached yet.
     */
    public static SafeOnlineConfig load(String propertiesPath) {

        if (config != null)
            return config;

        config = new SafeOnlineConfig();

        try {
            config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesPath));
        } catch (IOException e) {
            LOG.error("Couldn't load config file.", e);
        }

        return config;
    }

    /**
     * Get a reference to the web application configuration.
     */
    public static SafeOnlineConfig load() {

        return SafeOnlineConfig.load(SAFEONLINE_CONFIG);
    }

    /**
     * Create the {@link SafeOnlineConfig} based off of the configuration of the given {@link ServletTestManager}.
     * 
     * <p>
     * The {@link ServletTestManager} must already have been set up.
     * </p>
     * 
     * <p>
     * <b>Note:</b> Unlike the {@link #load()} and {@link #load(String)} methods, this one throws away any old {@link SafeOnlineConfig} and
     * rebuilds it either way.
     * </p>
     */
    public static SafeOnlineConfig load(ServletTestManager servletTestManager)
            throws Exception {

        config = new SafeOnlineConfig();
        URI servletLocation = URI.create(servletTestManager.getServletLocation());
        config.setProperty(OLAS_NODE_NAME, "olas-test");
        config.setProperty(OLAS_HOST_NAME, servletLocation.getHost());
        config.setProperty(OLAS_HOST_PORT, String.valueOf(servletLocation.getPort()));
        config.setProperty(OLAS_HOST_PORT_SSL, String.valueOf(servletLocation.getPort()));
        config.setProperty(OLAS_HOST_PROTOCOL, servletLocation.getScheme());
        config.setProperty(OLAS_HOST_PROTOCOL_SSL, servletLocation.getScheme());

        config.setProperty(APPBASE, new URI(servletLocation.getScheme(), servletLocation.getAuthority(), "/", null, null).toASCIIString());
        config.setProperty(APPLANDINGBASE,
                new URI(servletLocation.getScheme(), servletLocation.getAuthority(), "/", null, null).toASCIIString());

        config.setProperty(AUTHBASE, servletLocation.toASCIIString());
        config.setProperty(WSBASE, servletLocation.toASCIIString());

        return config;
    }

    /**
     * Create the {@link SafeOnlineConfig} based off of the configuration of the given {@link ServletTestManager} and
     * {@link WebServiceTestUtils}.
     * 
     * @see #load(ServletTestManager)
     */
    public static SafeOnlineConfig load(ServletTestManager servletTestManager, WebServiceTestUtils webServiceTestUtils)
            throws Exception {

        load(servletTestManager);
        config.setProperty(SafeOnlineConfig.WSBASE, webServiceTestUtils.getLocation());

        return config;
    }

    private static String getValue(HttpServletRequest request, String paramName) {

        String value = request.getSession().getServletContext().getInitParameter(paramName);
        if (value == null || value.length() == 0)
            throw new IllegalStateException(paramName + " not configured in web.xml!");

        return value;
    }
}
