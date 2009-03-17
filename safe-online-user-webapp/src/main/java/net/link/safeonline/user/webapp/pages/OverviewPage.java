/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages;

import net.link.safeonline.user.webapp.pages.profile.ProfilePage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.link.PageLink;


@RequireLogin(loginPage = MainPage.class)
public class OverviewPage extends UserTemplatePage {

    static final Log           LOG                  = LogFactory.getLog(OverviewPage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String PATH                 = "overview";

    public static final String PROFILE_LINK_ID      = "profile";
    public static final String APPLICATIONS_LINK_ID = "applications";
    public static final String DEVICES_LINK_ID      = "devices";
    public static final String ACCOUNT_LINK_ID      = "account";


    public OverviewPage() {

        super(Panel.home);

        getContent().add(new PageLink<String>(PROFILE_LINK_ID, ProfilePage.class));
        getContent().add(new PageLink<String>(APPLICATIONS_LINK_ID, OverviewPage.class));
        getContent().add(new PageLink<String>(DEVICES_LINK_ID, OverviewPage.class));
        getContent().add(new PageLink<String>(ACCOUNT_LINK_ID, OverviewPage.class));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("overview");
    }

}
