/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.node.util;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.util.servlet.SafeOnlineConfig;


/**
 * <h2>{@link NodeUtils}<br>
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
public abstract class NodeUtils {

    /**
     * @return The endpoint URL of the web application that responded to the given servlet request in the local node.
     */
    public static String getLocalNodeEndpoint(HttpServletRequest request)
            throws NodeNotFoundException {

        return getLocalNode().getLocation() + SafeOnlineConfig.webappPath(request);
    }

    /**
     * @return The {@link NodeEntity} describing the node on this machine (identified by {@link SafeOnlineNodeKeyStore}).
     */
    public static NodeEntity getLocalNode()
            throws NodeNotFoundException {

        try {
            NodeAuthenticationService nodeAuthenticationService = EjbUtils.getEJB(NodeAuthenticationService.JNDI_BINDING,
                    NodeAuthenticationService.class);
            return nodeAuthenticationService.getLocalNode();
        }

        catch (RuntimeException e) {
            if (e.getCause() instanceof NamingException)
                throw new NodeNotFoundException("Node services not available.");

            throw e;
        }
    }

    /**
     * @return The absolute URL for the path in the value of the given context parameter that points to the local node.
     */
    public static String absoluteLocalNodeUrlForParam(HttpServletRequest request, String contextParam)
            throws NodeNotFoundException {

        String path = request.getSession().getServletContext().getInitParameter(contextParam);
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }

        return getLocalNodeEndpoint(request) + path;
    }
}
