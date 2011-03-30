/*
 * SafeOnline project.
 *
 * Copyright 2006-2010 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.servlet;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import net.link.safeonline.sdk.configuration.ConfigUtils;
import net.link.util.j2ee.AbstractEJBInjectionServlet;
import net.link.util.j2ee.AbstractJBoss6EJBInjectionServlet;
import net.link.util.servlet.AbstractInjectionServlet;


/**
 * <h2>{@link AbstractEJBInjectionServlet}<br>
 * <sub>An {@link AbstractInjectionServlet} that also performs {@link EJB} injections.</sub></h2>
 *
 * <p>
 * <i>Jan 1, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class AbstractEJBLinkIDInjectionServlet extends AbstractJBoss6EJBInjectionServlet {

    protected String getWrapperEndpoint(HttpServletRequest request) {

        return ConfigUtils.getApplicationURL();
    }
}
