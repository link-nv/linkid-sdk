/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.servlet;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.util.servlet.AbstractInjectionServlet;


/**
 * Abstract Injection Servlet.
 * <p/>
 * <ul>
 * <li>Injects request parameters into servlet fields.
 * <li>Injects and outjects session parameters.
 * <li>Injects EJBs.
 * <li>Injects servlet init parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>Injects servlet context parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>By default checks if the servlet is accessed with a secure connection. If context parameter <code>Protocol</code> is
 * <code>http</code> or <code>securityCheck</code> is set to <code>false</code> this check will be ommitted.
 * </ul>
 *
 * @author fcorneli
 */
public abstract class AbstractLinkIDInjectionServlet extends AbstractInjectionServlet {

    protected String getWrapperEndpoint(HttpServletRequest request) {

        return ConfigUtils.getApplicationURL();
    }
}
