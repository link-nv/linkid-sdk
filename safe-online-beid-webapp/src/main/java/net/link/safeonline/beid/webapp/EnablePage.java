/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.beid.webapp;

import java.applet.Applet;

import org.apache.wicket.PageParameters;


/**
 * <h2>{@link EnablePage}<br>
 * <sub>Re-enable the PKCS11/PCSC SmartCard device.</sub></h2>
 * 
 * <p>
 * <i>Dec 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class EnablePage extends AppletPage {

    public EnablePage(PageParameters parameters) {

        super(parameters, getAppletClass(parameters), getAppletArchive(parameters), 450, 200, "beid", "_identity_enable", "_device_exit",
              "helpdesk", "./help_auth.seam?location=beid-applet.seam", "_no_pkcs11");

        // Header & Sidebar.
        getHeader();
        getSidebar();
    }

    static Class<? extends Applet> getAppletClass(PageParameters parameters) {

        if (isPkcs11(parameters))
            return net.link.safeonline.sc.pkcs11.identity.IdentityApplet.class;

        return net.link.safeonline.sc.pcsc.identity.IdentityApplet.class;
    }

    static String getAppletArchive(PageParameters parameters) {

        if (isPkcs11(parameters))
            return "safe-online-beid-pkcs11-applet-package.jar";

        return "safe-online-beid-pcsc-applet-package.jar";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCancelVisible() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cancel() {

        // No cancel button.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("%l", "beidEnable");
    }
}
