/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import net.link.safeonline.auth.protocol.LogoutServiceManager;
import net.link.safeonline.auth.servlet.LogoutExitServlet;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.authentication.service.LogoutState;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.time.Duration;


public class SSOLogoutPage extends AuthenticationTemplatePage {

    static final Log           LOG                         = LogFactory.getLog(SSOLogoutPage.class);

    private static final long  serialVersionUID            = 1L;

    /**
     * Determines how often (in seconds) the page will check the SSO logout progress OLAS by refreshing the {@link #LOGOUT_FORM} using AJAX.
     */
    private static final int   SSO_PROGRESS_CHECK_INTERVAL = 2;

    public static final String PATH                        = "ssologout";
    public static final String LOGOUT_FORM                 = "logoutform";


    public SSOLogoutPage() {

        getSidebar(localize("helpSSOLogout"));

        getHeader();

        getContent().add(new LogoutForm(LOGOUT_FORM));

        add(JavascriptPackageResource.getHeaderContribution("ssoLogout.js"));
        add(new AbstractHeaderContributor() {

            private static final long serialVersionUID = 1L;


            @Override
            public IHeaderContributor[] getHeaderContributors() {

                return new IHeaderContributor[] { new IHeaderContributor() {

                    private static final long serialVersionUID = 1L;


                    public void renderHead(IHeaderResponse response) {

                        response.renderOnDomReadyJavascript(String.format("beginLogout"));
                    }
                } };
            }
        });

        getContent().add(new ListView<ApplicationEntity>("applicationFrames", getLogoutService().getSsoApplicationsToLogout()) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(ListItem<ApplicationEntity> item) {

                final ApplicationEntity application = item.getModelObject();
                item.add(new InlineFrame("frame", new WebPage() {

                    @Override
                    protected void onBeforeRender() {

                        String logoutExitServletPath = WicketUtil.getHttpSession().getServletContext().getInitParameter(
                                LogoutExitServlet.PATH_CONTEXT_PARAM);

                        throw new RedirectToUrlException(String.format("%s?%s=%d", logoutExitServletPath,
                                LogoutExitServlet.APPLICATION_ID_GET_PARAMETER, application.getId()));
                    }
                }));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("ssoLogout");
    }


    class LogoutForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public LogoutForm(String id) {

            super(id);

            setOutputMarkupId(true);
            add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(SSO_PROGRESS_CHECK_INTERVAL)));

            add(new ListView<ApplicationEntity>("applications", getLogoutService().getSsoApplicationsToLogout()) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<ApplicationEntity> item) {

                    ApplicationEntity application = item.getModelObject();
                    LogoutState logoutState = getLogoutService().getSSoApplicationState(application);
                    String sessionStatusImage = "";
                    String progressStatusImage = "";
                    switch (logoutState) {
                        case INITIALIZED:
                            sessionStatusImage = "images/icons/door_open.png";
                            progressStatusImage = "";
                        break;

                        case INITIATED:
                        case LOGGING_OUT:
                            sessionStatusImage = "images/icons/door_open.png";
                            progressStatusImage = "images/icons/hourglass.png";
                        break;

                        case LOGOUT_FAILED:
                            sessionStatusImage = "images/icons/door_open.png";
                            progressStatusImage = "images/icons/error.png";
                        break;

                        case LOGOUT_SUCCESS:
                            sessionStatusImage = "images/icons/door.png";
                            progressStatusImage = "images/icons/tick.png";
                        break;
                    }

                    item.add(new Label("name", application.getFriendlyName()));
                    item.add(new Image("session", sessionStatusImage));
                    item.add(new Image("progress", progressStatusImage));
                }
            });
        }
    }


    LogoutService getLogoutService() {

        return LogoutServiceManager.getLogoutService(WicketUtil.getHttpSession());
    }
}
