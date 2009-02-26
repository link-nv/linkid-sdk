package net.link.safeonline.password.webapp.servlet;


import net.link.safeonline.device.sdk.auth.servlet.LandingServlet;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;


public class AuthLandingServlet extends LandingServlet {

    /**
     * @{inheritDoc}
     */
    protected OlasKeyStore getOlasKeyStore() {

        return new SafeOnlineNodeKeyStore();
    }
}
