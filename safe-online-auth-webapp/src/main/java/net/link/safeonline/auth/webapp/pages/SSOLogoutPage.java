/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import java.io.IOException;
import java.util.List;

import net.link.safeonline.auth.protocol.LogoutServiceManager;
import net.link.safeonline.auth.servlet.LogoutExitServlet;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.authentication.service.LogoutState;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.wicket.tools.RedirectResponseException;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;


public class SSOLogoutPage extends AuthenticationTemplatePage {

    static final Log           LOG                         = LogFactory.getLog(SSOLogoutPage.class);

    private static final long  serialVersionUID            = 1L;

    /**
     * Determines how often (in seconds) the page will check the SSO logout progress OLAS by refreshing the {@link #LOGOUT_FORM} using AJAX.
     */
    private static final int   SSO_PROGRESS_CHECK_INTERVAL = 1;

    public static final String PATH                        = "ssologout";
    public static final String LOGOUT_FORM                 = "logoutForm";


    public SSOLogoutPage() {

        // Link in case there is no JS.
        add(new Link<String>("noScriptLogout") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                String logoutExitServletPath = WicketUtil.getHttpSession().getServletContext().getInitParameter(
                        LogoutExitServlet.PATH_CONTEXT_PARAM);

                throw new RedirectToUrlException(logoutExitServletPath);
            }
        });
        // JS that unhides the JS-specific part.
        add(new AbstractHeaderContributor() {

            private static final long serialVersionUID = 1L;


            @Override
            public IHeaderContributor[] getHeaderContributors() {

                return new IHeaderContributor[] { new IHeaderContributor() {

                    private static final long serialVersionUID = 1L;


                    public void renderHead(IHeaderResponse response) {

                        response.renderOnDomReadyJavascript(String.format("document.getElementById('%s').style.display = 'block';"
                                + "document.getElementById('%s').style.visibility = 'visible';", "parallelLogout", "parallelLogout"));
                    }
                } };
            }
        });

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

        List<ApplicationEntity> ssoApplicationsToLogout = null;
        synchronized (WicketUtil.getHttpSession()) {
            ssoApplicationsToLogout = getLogoutService().getSsoApplicationsToLogout();
        }
        getContent().add(new ListView<ApplicationEntity>("applicationFrames", ssoApplicationsToLogout) {

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

        return localize("ssoLogoutTitle");
    }


    class LogoutForm extends Form<String> {

        private static final long serialVersionUID = 1L;
        private Button            logoutButton;


        public LogoutForm(String id) {

            super(id);

            setOutputMarkupId(true);
            add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(SSO_PROGRESS_CHECK_INTERVAL)) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void onPostProcessTarget(AjaxRequestTarget target) {

                    // Submit the form when logout is complete.
                    synchronized (WicketUtil.getHttpSession()) {
                        if (!getLogoutService().isPartial()) {
                            target.appendJavascript(String.format("document.getElementById('%s').submit();", getMarkupId()));
                        }
                    }
                }
            });

            add(new ListView<ApplicationEntity>("applications", new AbstractReadOnlyModel<List<ApplicationEntity>>() {

                private static final long serialVersionUID = 1L;


                @Override
                public List<ApplicationEntity> getObject() {

                    synchronized (WicketUtil.getHttpSession()) {
                        return getLogoutService().getSsoApplicationsToLogout();
                    }
                }
            }) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<ApplicationEntity> item) {

                    ApplicationEntity application = item.getModelObject();
                    LogoutState logoutState = null;
                    synchronized (WicketUtil.getHttpSession()) {
                        logoutState = getLogoutService().getSSoApplicationState(application);
                    }

                    String applicationName = application.getFriendlyName();
                    if (applicationName == null || applicationName.length() == 0) {
                        applicationName = application.getName();
                    }

                    String sessionStatusImage = "";
                    String progressStatusImage = "";
                    switch (logoutState) {
                        case INITIALIZED:
                            sessionStatusImage = "/images/icons/door_open.png";
                            progressStatusImage = "";
                        break;

                        case INITIATED:
                        case LOGGING_OUT:
                            sessionStatusImage = "/images/icons/door_open.png";
                            progressStatusImage = "/images/icons/hourglass.png";
                        break;

                        case LOGOUT_FAILED:
                            sessionStatusImage = "/images/icons/door_open.png";
                            progressStatusImage = "/images/icons/error.png";
                        break;

                        case LOGOUT_SUCCESS:
                            sessionStatusImage = "/images/icons/door.png";
                            progressStatusImage = "/images/icons/tick.png";
                        break;
                    }

                    Image sessionStatus = new Image("session", "override");
                    sessionStatus.add(new SimpleAttributeModifier("src", WicketUtil.getServletRequest().getContextPath()
                            + sessionStatusImage));
                    sessionStatus.setVisible(sessionStatusImage.length() > 0);
                    item.add(sessionStatus);
                    Image progressStatus = new Image("progress", "override");
                    progressStatus.add(new SimpleAttributeModifier("src", WicketUtil.getServletRequest().getContextPath()
                            + progressStatusImage));
                    progressStatus.setVisible(progressStatusImage.length() > 0);

                    item.add(new Label("name", applicationName), sessionStatus, progressStatus);
                }
            });

            add(logoutButton = new Button("logout", new Model<String>()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Change the logout button string once logout it complete.
            synchronized (WicketUtil.getHttpSession()) {
                if (getLogoutService().isPartial()) {
                    logoutButton.setModelObject(localize("interruptLogout"));
                } else {
                    logoutButton.setModelObject(localize("completeLogout"));
                }
            }

            super.onBeforeRender();
        }

        @Override
        protected void onSubmit() {

            throw new RedirectResponseException(new IRequestTarget() {

                public void detach(RequestCycle requestCycle) {

                }

                public void respond(RequestCycle requestCycle) {

                    requestCycle.getResponse().reset();

                    try {
                        LogoutExitServlet.logoutComplete(WicketUtil.getServletRequest(), WicketUtil.getServletResponse());
                    } catch (IOException e) {
                        throw new InternalInconsistencyException("Redirect failed.", e);
                    }
                }
            });
        }
    }


    LogoutService getLogoutService() {

        return LogoutServiceManager.getLogoutService(WicketUtil.getHttpSession());
    }
}
