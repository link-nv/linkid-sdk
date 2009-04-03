/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages;

import java.util.Date;
import java.util.List;
import java.util.Map;

import net.link.safeonline.sdk.exception.ApplicationPoolNotFoundException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.OlasServiceFactory;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.session.SessionAssertion;
import net.link.safeonline.user.keystore.UserKeyStore;
import net.link.safeonline.user.webapp.UserSession;
import net.link.safeonline.user.webapp.pages.account.AccountPage;
import net.link.safeonline.user.webapp.pages.applications.ApplicationsPage;
import net.link.safeonline.user.webapp.pages.devices.DevicesPage;
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
        getContent().add(new PageLink<String>(APPLICATIONS_LINK_ID, ApplicationsPage.class));
        getContent().add(new PageLink<String>(DEVICES_LINK_ID, DevicesPage.class));
        getContent().add(new PageLink<String>(ACCOUNT_LINK_ID, AccountPage.class));

        try {
            List<SessionAssertion> assertions = OlasServiceFactory.getSessionTrackingService(UserKeyStore.getPrivateKeyEntry())
                                                                  .getAssertions(UserSession.get().getSession(),
                                                                          UserSession.get().getUserId(), null);
            for (SessionAssertion assertion : assertions) {
                LOG.debug("assertion: subject=" + assertion.getSubject() + " pool=" + assertion.getApplicationPool());
                for (Map.Entry<Date, String> authenticationEntry : assertion.getAuthentications().entrySet()) {
                    LOG.debug("  * authentication: time=" + authenticationEntry.getKey() + " device=" + authenticationEntry.getValue());
                }
            }
        } catch (WSClientTransportException e) {
            LOG.error("[TODO]", e);
        } catch (ApplicationPoolNotFoundException e) {
            LOG.error("[TODO]", e);
        } catch (SubjectNotFoundException e) {
            LOG.error("[TODO]", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("overview");
    }

}
