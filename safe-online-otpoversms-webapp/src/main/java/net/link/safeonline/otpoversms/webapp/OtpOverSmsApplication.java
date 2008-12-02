/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;


public class OtpOverSmsApplication extends OlasApplication {

    static final Log LOG = LogFactory.getLog(OtpOverSmsApplication.class);


    @Override
    protected void init() {

        super.init();

        mountBookmarkablePage("authentication", AuthenticationPage.class);
        mountBookmarkablePage("registration", RegistrationPage.class);
        mountBookmarkablePage("removal", DisablePage.class);
        mountBookmarkablePage("update", UpdatePage.class);
        mountBookmarkablePage("disable", DisablePage.class);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {

        return new OtpOverSmsSession(request);

    }

}
