/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.webapp;

import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;


public class PasswordApplication extends OlasApplication {

    static final Log LOG = LogFactory.getLog(PasswordApplication.class);


    @Override
    protected void init() {

        super.init();

        mountBookmarkablePage("authentication", AuthenticationPage.class);
        mountBookmarkablePage("registration", RegistrationPage.class);
        mountBookmarkablePage("update", UpdatePage.class);
        mountBookmarkablePage("enable", EnablePage.class);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return null;
    }
}
