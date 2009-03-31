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


public class GlobalConfirmationPage extends AuthenticationTemplatePage {

    static final Log           LOG                     = LogFactory.getLog(GlobalConfirmationPage.class);

    private static final long  serialVersionUID        = 1L;

    public static final String PATH                    = "global-confirmation";

    public static final String USAGE_AGREEMENT_TEXT_ID = "usageAgreementText";

    public static final String CONFIRMATION_FORM_ID    = "confirmation_form";
    public static final String CONFIRM_BUTTON_ID       = "confirm";
    public static final String REJECT_BUTTON_ID        = "reject";

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    UsageAgreementService      usageAgreementService;


    public GlobalConfirmationPage() {

        getSidebar(localize("helpGlobalUsageAgreement"));

        getHeader();

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.agreements));

        final String text = usageAgreementService.getGlobalUsageAgreementText(getLocale().getLanguage());
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

        getContent().add(new ConfirmationForm(CONFIRMATION_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
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

                    LOG.debug("confirm global usage agreement");
                    usageAgreementService.confirmGlobalUsageAgreementVersion();
                    HelpdeskLogger.add("confirmed global usage agreement", LogLevelType.INFO);

                    getResponse().redirect(LoginServlet.SERVLET_PATH);
                    setRedirect(false);

                }
            });

            add(new Button(REJECT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    throw new RestartResponseException(new GlobalConfirmationRejectionPage());

                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
