/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.common;

/**
 * <h2>{@link SafeOnlineAppConstants}<br>
 * <sub>Constants for Safe-Online applications.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 7, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public interface SafeOnlineAppConstants {

    /*
     * Application Style.
     */
    /**
     * The 24-bit base color to use for the OLAS authentication web application's style sheet.
     * 
     * <p>
     * Accepted values are all encoded integers. HTML color codes are recommended <i>(for example: <code>#3333CC</code>)</i>.
     * </p>
     */
    public static final String COLOR_CONTEXT_PARAM       = "ApplicationColor";

    /**
     * Determines whether the OLAS authentication webapp will be displayed in-line. This will cause it to change its style sheet accordingly
     * (for instance, hide its headers and footers).
     * 
     * <ul>
     * <li>True</li>
     * <li>False <i>[default]</i></li>
     * </ul>
     */
    public static final String MINIMAL_CONTEXT_PARAM     = "ApplicationMinimal";

    public static final String COLOR_SESSION_ATTRIBUTE   = "applicationColor";
    public static final String MINIMAL_SESSION_ATTRIBUTE = "applicationMinimal";
}
