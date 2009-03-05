/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.webapp;

import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.webapp.converter.PhoneNumberConverter;
import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.util.convert.ConverterLocator;


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
    protected IConverterLocator newConverterLocator() {

        ConverterLocator converterLocator = new ConverterLocator();
        converterLocator.set(PhoneNumber.class, new PhoneNumberConverter());

        return converterLocator;
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

        return new EncapSession(request);
    }
}
