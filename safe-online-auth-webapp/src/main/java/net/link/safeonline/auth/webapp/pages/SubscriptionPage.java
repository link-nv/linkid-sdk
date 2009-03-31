/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import javax.ejb.EJB;

import net.link.safeonline.auth.servlet.LoginServlet;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;


public class SubscriptionPage extends AuthenticationTemplatePage {

    static final Log           LOG                     = LogFactory.getLog(SubscriptionPage.class);

    private static final long  serialVersionUID        = 1L;

    public static final String PATH                    = "subscription";

    public static final String USAGE_AGREEMENT_TEXT_ID = "usageAgreementText";

    public static final String SUBSCRIPTION_FORM_ID    = "subscription_form";
    public static final String CONFIRM_BUTTON_ID       = "confirm";
    public static final String SUBSCRIBE_BUTTON_ID     = "subscribe";
    public static final String REJECT_BUTTON_ID        = "reject";

    @EJB(mappedName = SubscriptionService.JNDI_BINDING)
    SubscriptionService        subscriptionService;

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    UsageAgreementService      usageAgreementService;

    String                     text;


    public SubscriptionPage() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());

        getSidebar(localize("helpSubscriptionConfirm"));

        getHeader();

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.agreements));

        try {
            text = usageAgreementService.getUsageAgreementText(protocolContext.getApplicationId(), getLocale().getLanguage());
        } catch (ApplicationNotFoundException e) {
            error(localize("errorApplicationNotFound"));
            return;
        }
        getContent().add(new Label(USAGE_AGREEMENT_TEXT_ID, text) {

            private static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isVisible() {

                return null != text;
            }
        });

        getContent().add(new ConfirmationForm(SUBSCRIPTION_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        String title = localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
        return title;
    }


    class ConfirmationForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        @SuppressWarnings("unchecked")
        public ConfirmationForm(String id) {

            super(id);
            setMarkupId(id);

            add(new Button(CONFIRM_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    subscribe();

                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean isVisible() {

                    return null != text;
                }
            });

            add(new Button(SUBSCRIBE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onSubmit() {

                    subscribe();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean isVisible() {

                    return null == text;
                }

            });

            add(new Button(REJECT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    throw new RestartResponseException(new SubscriptionRejectionPage());

                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }


    void subscribe() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());

        try {
            if (!subscriptionService.isSubscribed(protocolContext.getApplicationId())) {
                LOG.debug("subscribe to application " + protocolContext.getApplicationName());
                subscriptionService.subscribe(protocolContext.getApplicationId());
                HelpdeskLogger.add("subscribed to application: " + protocolContext.getApplicationFriendlyName(), LogLevelType.INFO);
            }

            if (usageAgreementService.requiresUsageAgreementAcceptation(protocolContext.getApplicationId(), getLocale().getLanguage())) {
                LOG.debug("confirm usage agreement for application " + protocolContext.getApplicationName());
                usageAgreementService.confirmUsageAgreementVersion(protocolContext.getApplicationId());
                HelpdeskLogger.add("confirmed usage agreement of application: " + protocolContext.getApplicationFriendlyName(),
                        LogLevelType.INFO);
            }
        } catch (ApplicationNotFoundException e) {
            error(localize("errorApplicationNotFound"));
            return;
        } catch (AlreadySubscribedException e) {
            error(localize("errorAlreadySubscribed"));
            return;
        } catch (PermissionDeniedException e) {
            error(localize("errorPermissionDenied"));
            return;
        } catch (SubscriptionNotFoundException e) {
            error(localize("errorSubscriptionNotFound"));
            return;
        }

        getResponse().redirect(LoginServlet.SERVLET_PATH);
        setRedirect(false);
    }

}
