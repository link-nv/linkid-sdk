/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.node.util;

import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link AbstractNodeInjectionServlet}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 20, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractNodeInjectionServlet extends AbstractInjectionServlet {

    private static final long serialVersionUID = 1L;
    private static final Log  LOG              = LogFactory.getLog(AbstractNodeInjectionServlet.class);


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWrapperEndpoint(HttpServletRequest request) {

        try {
            return NodeUtils.getLocalNodeEndpoint(request);
        }

        catch (NodeNotFoundException e) {
            LOG.error("Expected to be in a node, but node wasn't found: falling back to default method of endpoint retrieval.", e);
        }

        return super.getWrapperEndpoint(request);
    }
}
