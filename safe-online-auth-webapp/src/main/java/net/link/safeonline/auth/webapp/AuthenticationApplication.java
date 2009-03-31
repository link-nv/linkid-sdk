/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp;

import net.link.safeonline.auth.webapp.pages.AllDevicesPage;
import net.link.safeonline.auth.webapp.pages.AuthenticationProtocolErrorPage;
import net.link.safeonline.auth.webapp.pages.DeviceErrorPage;
import net.link.safeonline.auth.webapp.pages.FirstTimePage;
import net.link.safeonline.auth.webapp.pages.GlobalConfirmationPage;
import net.link.safeonline.auth.webapp.pages.IdentityConfirmationPage;
import net.link.safeonline.auth.webapp.pages.IdentityUnavailablePage;
import net.link.safeonline.auth.webapp.pages.IndexPage;
import net.link.safeonline.auth.webapp.pages.MainPage;
import net.link.safeonline.auth.webapp.pages.MissingAttributesPage;
import net.link.safeonline.auth.webapp.pages.NewUserDevicePage;
import net.link.safeonline.auth.webapp.pages.RegisterDevicePage;
import net.link.safeonline.auth.webapp.pages.SSOLogoutPage;
import net.link.safeonline.auth.webapp.pages.SubscriptionPage;
import net.link.safeonline.auth.webapp.pages.TimeoutPage;
import net.link.safeonline.auth.webapp.pages.UnsupportedProtocolPage;
import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;


public class AuthenticationApplication extends OlasApplication {

    static final Log LOG = LogFactory.getLog(AuthenticationApplication.class);


    @Override
    protected void init() {

        super.init();

        mountBookmarkablePage(FirstTimePage.PATH, FirstTimePage.class);
        mountBookmarkablePage(MainPage.PATH, MainPage.class);
        mountBookmarkablePage(NewUserDevicePage.PATH, NewUserDevicePage.class);
        mountBookmarkablePage(AllDevicesPage.PATH, AllDevicesPage.class);
        mountBookmarkablePage(UnsupportedProtocolPage.PATH, UnsupportedProtocolPage.class);
        mountBookmarkablePage(AuthenticationProtocolErrorPage.PATH, AuthenticationProtocolErrorPage.class);
        mountBookmarkablePage(DeviceErrorPage.PATH, DeviceErrorPage.class);
        mountBookmarkablePage(TimeoutPage.PATH, TimeoutPage.class);
        mountBookmarkablePage(RegisterDevicePage.PATH, RegisterDevicePage.class);
        mountBookmarkablePage(GlobalConfirmationPage.PATH, GlobalConfirmationPage.class);
        mountBookmarkablePage(SubscriptionPage.PATH, SubscriptionPage.class);
        mountBookmarkablePage(IdentityConfirmationPage.PATH, IdentityConfirmationPage.class);
        mountBookmarkablePage(IdentityUnavailablePage.PATH, IdentityUnavailablePage.class);
        mountBookmarkablePage(MissingAttributesPage.PATH, MissingAttributesPage.class);
        mountBookmarkablePage(SSOLogoutPage.PATH, SSOLogoutPage.class);

        getApplicationSettings().setPageExpiredErrorPage(TimeoutPage.class);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page> getHomePage() {

        return IndexPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean jaasLogin() {

        return true;
    }
}
