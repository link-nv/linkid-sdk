/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.test.util;

import java.net.URI;
import java.net.URL;

import net.link.safeonline.util.servlet.SafeOnlineConfig;


/**
 * <h2>{@link SafeOnlineTestConfig}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 24, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class SafeOnlineTestConfig extends SafeOnlineConfig {

    private static final long serialVersionUID = 1L;


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
    public static SafeOnlineConfig loadTest(ServletTestManager servletTestManager)
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
     * Create the {@link SafeOnlineConfig} using the given properties.
     * 
     * <p>
     * <b>Note:</b> This method doesn't provide node configuration.
     * </p>
     * 
     * <p>
     * <b>Note:</b> Unlike the {@link #load()} and {@link #load(String)} methods, this one throws away any old {@link SafeOnlineConfig} and
     * rebuilds it either way.
     * </p>
     */
    public static SafeOnlineConfig loadTestApp(String appbase, String applandingbase, String authbase, String wsbase)
            throws Exception {

        config = new SafeOnlineConfig();
        config.setProperty(APPBASE, appbase);
        config.setProperty(APPLANDINGBASE, applandingbase);

        config.setProperty(AUTHBASE, authbase);
        config.setProperty(WSBASE, wsbase);

        return config;
    }

    /**
     * Create the {@link SafeOnlineConfig} using the given properties.
     * 
     * <p>
     * <b>Note:</b> This method doesn't provide application configuration.
     * </p>
     * 
     * <p>
     * <b>Note:</b> Unlike the {@link #load()} and {@link #load(String)} methods, this one throws away any old {@link SafeOnlineConfig} and
     * rebuilds it either way.
     * </p>
     */
    public static SafeOnlineConfig loadTestNode(URL nodeUrl)
            throws Exception {

        config = new SafeOnlineConfig();
        config.setProperty(OLAS_NODE_NAME, "olas-test");
        config.setProperty(OLAS_HOST_NAME, nodeUrl.getHost());
        config.setProperty(OLAS_HOST_PORT, String.valueOf(nodeUrl.getPort()));
        config.setProperty(OLAS_HOST_PORT_SSL, String.valueOf(nodeUrl.getPort()));
        config.setProperty(OLAS_HOST_PROTOCOL, nodeUrl.getProtocol());
        config.setProperty(OLAS_HOST_PROTOCOL_SSL, nodeUrl.getProtocol());

        config.setProperty(AUTHBASE, nodeUrl.toExternalForm() + "/olas-auth/");
        config.setProperty(WSBASE, nodeUrl.toExternalForm());

        return config;
    }

    /**
     * Create the {@link SafeOnlineConfig} based off of the configuration of the given {@link ServletTestManager} and
     * {@link WebServiceTestUtils}.
     * 
     * @see #loadTest(ServletTestManager)
     */
    public static SafeOnlineConfig loadTest(ServletTestManager servletTestManager, WebServiceTestUtils webServiceTestUtils)
            throws Exception {

        loadTest(servletTestManager);
        config.setProperty(SafeOnlineConfig.WSBASE, webServiceTestUtils.getLocation());

        return config;
    }
}
