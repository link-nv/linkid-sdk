/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.account;

import javax.ejb.EJB;

import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PageLink;


@RequireLogin(loginPage = MainPage.class)
public class UsagePage extends UserTemplatePage {

    static final Log           LOG                  = LogFactory.getLog(UsagePage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String PATH                 = "overview";

    public static final String ACCOUNT_SIDE_LINK_ID = "account_side";
    public static final String HISTORY_SIDE_LINK_ID = "history_side";
    public static final String REMOVE_SIDE_LINK_ID  = "remove_side";

    public static final String USAGE_TEXT           = "usage_text";

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    UsageAgreementService      usageAgreementService;

    @EJB(mappedName = SubjectManager.JNDI_BINDING)
    SubjectManager             subjectManager;


    public UsagePage() {

        super(Panel.account);

        getSidebar(localize("helpUsageAgreement"), false);

        getSidebar().add(new PageLink<String>(ACCOUNT_SIDE_LINK_ID, AccountPage.class));
        getSidebar().add(new PageLink<String>(HISTORY_SIDE_LINK_ID, HistoryPage.class));
        getSidebar().add(new PageLink<String>(REMOVE_SIDE_LINK_ID, RemovePage.class));

        getContent().add(
                new Label(USAGE_TEXT, usageAgreementService.getGlobalUsageAgreementText(getLocale().getLanguage(),
                        subjectManager.getCallerSubject().getConfirmedUsageAgreementVersion())));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("usageAgreementOLAS");
    }
}
