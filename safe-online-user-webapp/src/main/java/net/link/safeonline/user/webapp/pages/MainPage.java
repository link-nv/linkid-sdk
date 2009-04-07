/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.webapp.template.SideLink;
import net.link.safeonline.webapp.template.SidebarBorder;
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

        OlasLoginLink loginLink = new OlasLoginLink(SidebarBorder.LINK_ID, OverviewPage.class) {

            private static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            @Override
            protected void delegate(String target, HttpServletRequest request, HttpServletResponse response) {

                super.delegate(target, request, response);

                // set login cookie for timeout filter
                Cookie loginCookie = new Cookie(SafeOnlineCookies.LOGIN_COOKIE, "true");
                loginCookie.setPath(request.getContextPath());
                response.addCookie(loginCookie);
            }

        };

        OlasLogoutLink logoutLink = new OlasLogoutLink(SidebarBorder.LINK_ID, MainPage.class);

        if (OLASSession.get().isUserSet()) {
            getSidebar(localize("helpMain"), false, new SideLink(logoutLink, localize("logout")));
        } else {
            getSidebar(localize("helpMain"), false, new SideLink(loginLink, localize("loginaction")));
        }

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
