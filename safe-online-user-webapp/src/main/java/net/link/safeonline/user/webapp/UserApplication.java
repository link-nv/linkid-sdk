/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.webapp;

import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.pages.OverviewPage;
import net.link.safeonline.user.webapp.pages.devices.DeviceErrorPage;
import net.link.safeonline.user.webapp.pages.devices.DevicesPage;
import net.link.safeonline.webapp.common.TimeoutPage;
import net.link.safeonline.webapp.template.OlasApplication;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;


/**
 * <h2>{@link UserApplication}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 16, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class UserApplication extends OlasApplication {

    @Override
    protected void init() {

        super.init();

        mountBookmarkablePage(MainPage.PATH, MainPage.class);
        mountBookmarkablePage(OverviewPage.PATH, OverviewPage.class);
        mountBookmarkablePage(DevicesPage.PATH, DevicesPage.class);
        mountBookmarkablePage(DeviceErrorPage.PATH, DeviceErrorPage.class);

        getApplicationSettings().setPageExpiredErrorPage(TimeoutPage.class);

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

        return new UserSession(request);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean jaasLogin() {

        return true;
    }

}
