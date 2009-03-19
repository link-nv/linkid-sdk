/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.account;

import javax.ejb.EJB;
import javax.servlet.http.Cookie;

import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.user.webapp.UserSession;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.PageLink;


@RequireLogin(loginPage = MainPage.class)
public class RemovePage extends UserTemplatePage {

    static final Log           LOG                  = LogFactory.getLog(RemovePage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String PATH                 = "overview";

    public static final String ACCOUNT_SIDE_LINK_ID = "account_side";
    public static final String HISTORY_SIDE_LINK_ID = "history_side";
    public static final String USAGE_SIDE_LINK_ID   = "usage_side";

    public static final String REMOVE_FORM_ID       = "remove_form";
    public static final String REMOVE_BUTTON_ID     = "remove";
    public static final String CANCEL_BUTTON_ID     = "cancel";

    @EJB(mappedName = AccountService.JNDI_BINDING)
    AccountService             accountService;


    public RemovePage() {

        super(Panel.account);

        getSidebar(localize("helpRemoveAccount"), false);

        getSidebar().add(new PageLink<String>(ACCOUNT_SIDE_LINK_ID, AccountPage.class));
        getSidebar().add(new PageLink<String>(HISTORY_SIDE_LINK_ID, HistoryPage.class));
        getSidebar().add(new PageLink<String>(USAGE_SIDE_LINK_ID, UsagePage.class));

        getContent().add(new RemoveForm(REMOVE_FORM_ID));
    }


    class RemoveForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public RemoveForm(String id) {

            super(id);

            add(new Button(REMOVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("remove");
                    try {
                        accountService.removeAccount();
                    } catch (SubscriptionNotFoundException e) {
                        error(localize("errorSubscriptionNotFound"));
                        return;
                    } catch (MessageHandlerNotFoundException e) {
                        error(localize("errorMessage"));
                        return;
                    }
                    removeLoginCookie();
                    LoginManager.setUserId(null, WicketUtil.toServletRequest(getRequest()));
                    UserSession.get().setUserId(null);
                    setResponsePage(MainPage.class);
                }
            });

            add(new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("cancel");
                    setResponsePage(AccountPage.class);
                }

            }.setDefaultFormProcessing(false));

            add(new ErrorFeedbackPanel("feedback"));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onSubmit() {

            LOG.debug("form submit");
            super.onSubmit();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("removeAccount");
    }

    void removeLoginCookie() {

        Cookie loginCookie = new Cookie(SafeOnlineCookies.LOGIN_COOKIE, "");
        loginCookie.setPath(WicketUtil.toServletRequest(getRequest()).getContextPath());
        loginCookie.setMaxAge(0);
        WicketUtil.toServletResponse(getResponse()).addCookie(loginCookie);
    }
}
