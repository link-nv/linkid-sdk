/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class EncapApplication extends OlasApplication {

    public static final String ENABLE_MOUNTPOINT         = "enable";
    public static final String AUTHENTICATION_MOUNTPOINT = "authentication";
    public static final String REGISTRATION_MOUNTPOINT   = "registration";
    static final Log           LOG                       = LogFactory.getLog(EncapApplication.class);


    @Override
    protected void init() {

        super.init();

        mountBookmarkablePage(AUTHENTICATION_MOUNTPOINT, AuthenticationPage.class);
        mountBookmarkablePage(ENABLE_MOUNTPOINT, EnablePage.class);
        mountBookmarkablePage(REGISTRATION_MOUNTPOINT, RegistrationPage.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return MainPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {

        return new EncapSession(request);
    }
}
