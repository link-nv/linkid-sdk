/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.beid.servlet;

import static net.link.safeonline.beid.webapp.BeIdMountPoints.AuthenticationType.PCSC;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.AuthenticationType.PKCS11;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.NO_MIDDLEWARE;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.MountPoint.AUTHENTICATION;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.MountPoint.ERROR;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.device.sdk.auth.servlet.AbstractDeviceAuthenticationLandingServlet;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.node.util.NodeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link DeviceAuthenticationLandingServlet}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 19, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class DeviceAuthenticationLandingServlet extends AbstractDeviceAuthenticationLandingServlet {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(DeviceAuthenticationLandingServlet.class);

    @EJB(mappedName = NodeAuthenticationService.JNDI_BINDING)
    NodeAuthenticationService nodeAuthenticationService;


    /**
     * {@inheritDoc}
     */
    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("prepareJavaVersionCheck");
        HttpSession session = request.getSession();
        JavaVersionServlet.setPkcs11Target(AUTHENTICATION.linkFor(PKCS11), session);
        JavaVersionServlet.setJava15NoPkcs11Target(ERROR.linkFor(NO_MIDDLEWARE), session);
        JavaVersionServlet.setJava16NoPkcs11Target(AUTHENTICATION.linkFor(PCSC), session);

        super.invokePost(request, response);
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIssuer() {

        try {
            return nodeAuthenticationService.getLocalNode().getName();
        }

        catch (NodeNotFoundException e) {
            throw new InternalInconsistencyException("Couldn't look up local node.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OlasKeyStore getKeyStore() {

        return new SafeOnlineNodeKeyStore();
    }
}
