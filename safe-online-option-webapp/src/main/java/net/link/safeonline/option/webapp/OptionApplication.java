/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.webapp;

import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;


public class OptionApplication extends OlasApplication {

    static final Log           LOG                       = LogFactory.getLog(OptionApplication.class);
    public static final String AUTHENTICATION_MOUNTPOINT = "authentication";
    public static final String REGISTRATION_MOUNTPOINT   = "registration";
    public static final String UPDATE_MOUNTPOINT         = "update";


    @Override
    protected void init() {

        super.init();

        mountBookmarkablePage(AUTHENTICATION_MOUNTPOINT, AuthenticationPage.class);
        mountBookmarkablePage(REGISTRATION_MOUNTPOINT, RegistrationPage.class);
        mountBookmarkablePage(UPDATE_MOUNTPOINT, UpdatePage.class);
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

        return new OptionSession(request);
    }
}
