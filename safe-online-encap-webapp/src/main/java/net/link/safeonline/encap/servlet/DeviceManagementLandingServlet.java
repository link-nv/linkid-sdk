package net.link.safeonline.encap.servlet;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.device.sdk.manage.servlet.AbstractDeviceManagementLandingServlet;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.node.util.NodeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DeviceManagementLandingServlet extends AbstractDeviceManagementLandingServlet {

    private static final long serialVersionUID = 1L;
    private static final Log  LOG              = LogFactory.getLog(DeviceManagementLandingServlet.class);

    @EJB(mappedName = NodeAuthenticationService.JNDI_BINDING)
    NodeAuthenticationService nodeAuthenticationService;


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
     * @{inheritDoc
     */
    @Override
    protected OlasKeyStore getKeyStore() {

        return new SafeOnlineNodeKeyStore();
    }
}
