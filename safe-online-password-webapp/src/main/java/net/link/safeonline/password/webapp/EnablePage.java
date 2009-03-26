/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.CustomRequiredPasswordTextField;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;


public class EnablePage extends TemplatePage {

    static final Log           LOG               = LogFactory.getLog(EnablePage.class);

    private static final long  serialVersionUID  = 1L;

    public static final String ENABLE_FORM_ID    = "enable_form";
    public static final String PASSWORD_FIELD_ID = "password";
    public static final String ENABLE_BUTTON_ID  = "enable";
    public static final String CANCEL_BUTTON_ID  = "cancel";

    @EJB(mappedName = PasswordDeviceService.JNDI_BINDING)
    PasswordDeviceService      passwordDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;


    public EnablePage() {

        getHeader();
        getSidebar(localize("helpPasswordEnable"));
        getContent().add(new EnableForm(ENABLE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("enableUsernamePassword");
    }


    class EnableForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             password;


        @SuppressWarnings("unchecked")
        public EnableForm(String id) {

            super(id);

            final CustomRequiredPasswordTextField passwordField = new CustomRequiredPasswordTextField(PASSWORD_FIELD_ID,
                    password = new Model<String>());
            add(passwordField);
            passwordField.setRequiredMessageKey("errorMissingPassword");
            add(new ErrorComponentFeedbackLabel("password_feedback", passwordField, new Model<String>(localize("errorMissingPassword"))));
            focus(passwordField);

            add(new Button(ENABLE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
                    LOG.debug("enable password for " + protocolContext.getSubject());

                    try {
                        passwordDeviceService.enable(protocolContext.getSubject(), password.getObject());

                        protocolContext.setSuccess(true);
                        exit();
                    }

                    catch (SubjectNotFoundException e) {
                        EnableForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(), "enable: subject not found",
                                LogLevelType.ERROR);
                    } catch (DeviceAuthenticationException e) {
                        passwordField.error(getLocalizer().getString("errorPasswordNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(), "enable: permission denied: "
                                + e.getMessage(), LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        passwordField.error(getLocalizer().getString("errorPasswordNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(), "enable: device not found",
                                LogLevelType.ERROR);
                    }
                }
            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).setSuccess(false);
                    exit();
                }

            };
            cancel.setDefaultFormProcessing(false);
            add(cancel);

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }


    public void exit() {

        ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).setValidity(
                samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
