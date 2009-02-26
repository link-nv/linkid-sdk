package net.link.safeonline.password.webapp.servlet;


import net.link.safeonline.device.sdk.servlet.LandingServlet;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;


public class DeviceLandingServlet extends LandingServlet {

    /**
     * @{inheritDoc}
     */
    protected OlasKeyStore getOlasKeyStore() {

        return new SafeOnlineNodeKeyStore();
    }
}
