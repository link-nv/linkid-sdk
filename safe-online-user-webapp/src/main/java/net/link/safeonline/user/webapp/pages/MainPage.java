/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages;

import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.OLASSession;
import net.link.safeonline.wicket.web.OlasLoginLink;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;


public class MainPage extends UserTemplatePage {

    static final Log           LOG              = LogFactory.getLog(MainPage.class);

    private static final long  serialVersionUID = 1L;

    public static final String PATH             = "main";

    public static final String INTRO_MESSAGE_ID = "intro_message";
    public static final String LOGIN_LINK_ID    = "login";
    public static final String LOGOUT_LINK_ID   = "logout";


    public MainPage() {

        super(Panel.home);

        OlasLoginLink loginLink = new OlasLoginLink(LOGIN_LINK_ID, OverviewPage.class);
        loginLink.setVisible(!OLASSession.get().isUserSet());

        OlasLogoutLink logoutLink = new OlasLogoutLink(LOGOUT_LINK_ID, OverviewPage.class);
        logoutLink.setVisible(OLASSession.get().isUserSet());

        getSidebar(localize("helpMain"), false).add(loginLink, logoutLink);

        String commercialName = WicketUtil.toServletRequest(getRequest()).getSession().getServletContext().getInitParameter(
                "CommercialName");
        getContent().add(
                new Label(INTRO_MESSAGE_ID, new StringResourceModel("introMessage", this, new Model<MainPage>(this),
                        new Object[] { commercialName })));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return null;
    }

}
