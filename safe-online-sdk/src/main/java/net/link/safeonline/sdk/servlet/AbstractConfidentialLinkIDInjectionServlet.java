/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.servlet;

import javax.servlet.http.HttpServletRequest;
import net.link.safeonline.sdk.configuration.ConfigUtils;


/**
 * <h2>{@link AbstractConfidentialLinkIDInjectionServlet}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Mar 21, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class AbstractConfidentialLinkIDInjectionServlet extends AbstractLinkIDInjectionServlet {

    /**
     * <p> Landing servlets need to use the <b>landing</b>point for request wrapping. </p>
     *
     * {@inheritDoc}
     */
    @Override
    protected String getWrapperEndpoint(HttpServletRequest request) {

        return ConfigUtils.getApplicationConfidentialURL();
    }
}
