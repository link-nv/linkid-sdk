/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.account;

import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.link.PageLink;


@RequireLogin(loginPage = MainPage.class)
public class AccountPage extends UserTemplatePage {

    static final Log           LOG                  = LogFactory.getLog(AccountPage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String PATH                 = "overview";

    public static final String HISTORY_SIDE_LINK_ID = "history_side";
    public static final String USAGE_SIDE_LINK_ID   = "usage_side";
    public static final String REMOVE_SIDE_LINK_ID  = "remove_side";

    public static final String HISTORY_LINK_ID      = "history";
    public static final String USAGE_LINK_ID        = "usage";
    public static final String REMOVE_LINK_ID       = "remove";


    public AccountPage() {

        super(Panel.account);

        getSidebar(localize("helpAccountManagement"), false);

        getSidebar().add(new PageLink<String>(HISTORY_SIDE_LINK_ID, HistoryPage.class));
        getSidebar().add(new PageLink<String>(USAGE_SIDE_LINK_ID, UsagePage.class));
        getSidebar().add(new PageLink<String>(REMOVE_SIDE_LINK_ID, RemovePage.class));

        getContent().add(new PageLink<String>(HISTORY_LINK_ID, HistoryPage.class));
        getContent().add(new PageLink<String>(USAGE_LINK_ID, UsagePage.class));
        getContent().add(new PageLink<String>(REMOVE_LINK_ID, RemovePage.class));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("accountManagement");
    }

}
