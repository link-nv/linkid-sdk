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
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;


/**
 * <h2>{@link AuthenticationPage}<br>
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
public class AuthenticationPage extends AppletPage {

    private static final long      serialVersionUID = 1L;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;


    public AuthenticationPage(PageParameters parameters) {

        super(parameters, getAppletClass(parameters), getAppletArchive(parameters), 450, 200, "beid", "_authentication",
              "_authentication_exit", "helpdesk", "./help_auth.seam?location=beid-applet.seam", "_no_pkcs11");

        // Header & Sidebar.
        getHeader();
        getSidebar().add(new Link<String>("tryAnotherDevice") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                cancel();
            }
        });

        // Our content.
        ProgressAuthenticationPanel progress = new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate);
        getContent().add(progress);
        getContent().add(new Label("title", localize("%l %s", "authenticatingFor", authenticationContext.getApplicationFriendlyName())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("authenticateBeId");
    }

    static Class<? extends Applet> getAppletClass(PageParameters parameters) {

        if (isPkcs11(parameters))
            return net.link.safeonline.sc.pkcs11.auth.AuthenticationApplet.class;

        return net.link.safeonline.sc.pcsc.auth.AuthenticationApplet.class;
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

        authenticationContext.setUsedDevice(BeIdConstants.BEID_DEVICE_ID);
        authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

        throw new RedirectToUrlException("_authentication_exit");
    }
}
