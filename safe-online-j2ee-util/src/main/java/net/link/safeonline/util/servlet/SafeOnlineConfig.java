/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.util.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

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

    private static final long                    serialVersionUID          = 1L;
    private static final Log                     LOG                       = LogFactory.getLog(SafeOnlineConfig.class);
    private static Map<String, SafeOnlineConfig> configs;

    /**
     * The path to the configuration file to use for host-specific configuration.
     */
    public static final String                   CONFIG_CONTEXT_PARAM      = "SafeOnlineConfig";

    /**
     * The absolute base path for the web application.
     * 
     * <p>
     * <b>NOTE:</b> Put slashes before and after: /myapp/
     * </p>
     */
    public static final String                   WEBAPP_PATH_CONTEXT_PARAM = "WebappPath";


    /**
     * Property that defines the base URL for applications on this host.
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>http://my.host.be</code>) <i>[required]</i>
     * </p>
     */
    public String appbase() {

        return getProperty("appbase");
    }

    /**
     * Property that defines the base URL for application landing pages on this host (should be on HTTPS).
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>https://my.host.be</code>) <i>[required]</i>
     * </p>
     */
    public String applandingbase() {

        return getProperty("applandingbase");
    }

    /**
     * Property that defines the base URL to the OLAS authentication web application to use for application authentication.
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]/[path-to-olas-auth]</code> (eg. <code>https://my.olas.be/olas-auth</code>)
     * <i>[required]</i>
     * </p>
     */
    public String authbase() {

        return getProperty("authbase");
    }

    /**
     * Property that defines the base URL to the OLAS web services to use.
     * 
     * <p>
     * Use the form: <code>[scheme]//[authority]</code> (eg. <code>https://my.olas.be</code>) <i>[required]</i>
     * </p>
     */
    public String wsbase() {

        return getProperty("wsbase");
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
    public String getApplicationEndpointFor(HttpServletRequest request) {

        String webappPath = webappPath(request);

        return appbase() + webappPath;
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
    public String getApplicationLandingpointFor(HttpServletRequest request) {

        String webappPath = webappPath(request);

        return applandingbase() + webappPath;
    }

    /**
     * The path of the web application that services the given request.
     */
    public String webappPath(HttpServletRequest request) {

        return getValue(request, WEBAPP_PATH_CONTEXT_PARAM);
    }

    /**
     * <p>
     * <b>Note: Only for use by OLAS applications.</b> Node services should use their node's location to create the absolute URL.
     * </p>
     * 
     * @return An absolute URL within the current web application using the specified path.
     */
    public String absoluteApplicationUrlFromPath(HttpServletRequest request, String path) {

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
    public String absoluteApplicationUrlFromParam(HttpServletRequest request, String contextParamName) {

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
    public String absoluteApplicationLandingUrlFromPath(HttpServletRequest request, String path) {

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
    public String absoluteApplicationLandingUrlFromParam(HttpServletRequest request, String contextParamName) {

        LOG.debug("Looking up absolute landing path for " + contextParamName + " ..");
        return absoluteApplicationLandingUrlFromPath(request, request.getSession().getServletContext().getInitParameter(contextParamName));
    }

    /**
     * Load the web application configuration from the specified properties file if it's not cached yet.
     */
    public static SafeOnlineConfig load(String propertiesPath) {

        if (configs == null) {
            configs = new HashMap<String, SafeOnlineConfig>();
        }

        if (configs.containsKey(propertiesPath))
            return configs.get(propertiesPath);

        SafeOnlineConfig config = new SafeOnlineConfig();

        try {
            config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesPath));
            configs.put(propertiesPath, config);
        } catch (IOException e) {
            LOG.error("Couldn't load config file.", e);
        }

        LOG.debug("Successfully loaded SafeOnlineConfig with hostbase: " + config.keySet());
        return config;
    }

    /**
     * Get a reference to the web application configuration.
     */
    public static SafeOnlineConfig load(HttpServletRequest request) {

        return SafeOnlineConfig.load(getValue(request, CONFIG_CONTEXT_PARAM));
    }

    private static String getValue(HttpServletRequest request, String paramName) {

        String value = request.getSession().getServletContext().getInitParameter(paramName);
        if (value == null || value.length() == 0)
            throw new IllegalStateException(paramName + " not configured in web.xml!");

        return value;
    }
}
