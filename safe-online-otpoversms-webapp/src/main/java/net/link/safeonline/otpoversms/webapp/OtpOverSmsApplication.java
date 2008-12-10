/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

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


public class OtpOverSmsApplication extends OlasApplication {

    static final Log LOG = LogFactory.getLog(OtpOverSmsApplication.class);


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

        return new OtpOverSmsSession(request);

    }

}
