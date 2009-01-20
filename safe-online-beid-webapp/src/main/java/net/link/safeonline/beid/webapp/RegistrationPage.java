/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.webapp;

import java.applet.Applet;

import javax.ejb.EJB;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.webapp.template.ProgressRegistrationPanel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RedirectToUrlException;


/**
 * <h2>{@link RegistrationPage}<br>
 * <sub>Authenticate using PKCS11/PCSC SmartCard.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 6, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class RegistrationPage extends AppletPage {

    private static final long      serialVersionUID = 1L;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;


    public RegistrationPage(PageParameters parameters) {

        super(parameters, getAppletClass(parameters), getAppletArchive(parameters), 450, 200, "beid", "_identity", "_device_exit",
              "helpdesk", "./help.seam?location=register-beid.seam", "_no_pkcs11");

        // Header & Sidebar.
        getHeader();
        getSidebar(localize("helpExtractIdentity"));

        // Our content.
        ProgressRegistrationPanel progress = new ProgressRegistrationPanel("progress", ProgressRegistrationPanel.stage.register);
        getContent().add(progress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("%l", "register");
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

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cancel() {

        protocolContext.setSuccess(false);
        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

        throw new RedirectToUrlException("_device_exit");
    }
}
