package net.link.safeonline.option.servlet;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.device.sdk.servlet.LandingServlet;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;


public class DeviceLandingServlet extends LandingServlet {

    private static final long serialVersionUID = 1L;

    @EJB(mappedName = NodeAuthenticationService.JNDI_BINDING)
    NodeAuthenticationService nodeAuthenticationService;


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
