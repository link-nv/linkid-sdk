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
 * <h2>{@link IdentificationPage}<br>
 * <sub>Re-enable the PKCS11/PCSC SmartCard device.</sub></h2>
 * 
 * <p>
 * <i>Dec 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class IdentificationPage extends AppletPage {

    public IdentificationPage(PageParameters parameters) {

        super(parameters, getAppletClass(parameters), getAppletArchive(parameters), 450, 200, "beid", "_identification_data",
              "_identification_exit", "helpdesk", "./help.seam?location=beid-applet.seam", "_no_pkcs11");

        // Header & Sidebar.
        getHeader();
        getSidebar();
    }

    static Class<? extends Applet> getAppletClass(@SuppressWarnings("unused") PageParameters parameters) {

        // if (isPkcs11(parameters))
        // return null;

        return net.link.safeonline.sc.pcsc.identification.IdentificationApplet.class;
    }

    static String getAppletArchive(@SuppressWarnings("unused") PageParameters parameters) {

        // if (isPkcs11(parameters))
        // return null;

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

        return localize("%l", "beidIdentification");
    }
}
