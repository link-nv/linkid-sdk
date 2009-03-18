/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.common;

import java.io.FileReader;
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
     * Property in the SafeOnlineConfig that defines the host that we're running on.
     */
    public final String                          hostbase                  = getProperty("hostbase");

    /**
     * Property in the SafeOnlineConfig that defines the absolute URL to the olas-auth application base.
     */
    public final String                          authbase                  = getProperty("authbase");


    /**
     * The endpoint URL is the base URL at which the application runs.
     * 
     * @return The endpoint URL for the web application triggered by the given request.
     */
    public String endpointFor(HttpServletRequest request) {

        return hostbase + request.getSession().getServletContext().getInitParameter(WEBAPP_PATH_CONTEXT_PARAM);
    }

    /**
     * @return An absolute URL within the current web application using the specified path.
     */
    public String absoluteUrlFromPath(HttpServletRequest request, String path) {

        if (URI.create(path).isAbsolute())
            return path;

        return endpointFor(request) + path;
    }

    /**
     * @return An absolute URL within the current web application using the path specified in the given context parameter.
     */
    public String absoluteUrlFromParam(HttpServletRequest request, String contextParamName) {

        return absoluteUrlFromPath(request, request.getSession().getServletContext().getInitParameter(contextParamName));
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
            config.load(new FileReader(propertiesPath));
            configs.put(propertiesPath, config);
        } catch (IOException e) {
            LOG.error("Couldn't load config file.", e);
        }

        return config;
    }

    /**
     * Get a reference to the web application configuration.
     */
    public static SafeOnlineConfig load(HttpServletRequest request) {

        String propertiesPath = request.getSession().getServletContext().getInitParameter(CONFIG_CONTEXT_PARAM);

        return SafeOnlineConfig.load(propertiesPath);
    }
}
