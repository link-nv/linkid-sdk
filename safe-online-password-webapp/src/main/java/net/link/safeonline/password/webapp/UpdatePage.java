/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
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
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;


public class UpdatePage extends TemplatePage {

    static final Log           LOG                  = LogFactory.getLog(UpdatePage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String UPDATE_FORM_ID       = "update_form";

    public static final String OLDPASSWORD_FIELD_ID = "oldpassword";

    public static final String PASSWORD1_FIELD_ID   = "password1";

    public static final String PASSWORD2_FIELD_ID   = "password2";

    public static final String SAVE_BUTTON_ID       = "save";

    public static final String CANCEL_BUTTON_ID     = "cancel";

    @EJB(mappedName = PasswordDeviceService.JNDI_BINDING)
    PasswordDeviceService      passwordDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;

    ProtocolContext            protocolContext;


    public UpdatePage() {

        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());

        getHeader();
        getSidebar(localize("helpPasswordChange"));

        getContent().add(new RegistrationForm(UPDATE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("updateUsernamePassword");
    }


    class RegistrationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             oldpassword;

        Model<String>             password1;

        Model<String>             password2;


        @SuppressWarnings("unchecked")
        public RegistrationForm(String id) {

            super(id);

            final CustomRequiredPasswordTextField oldpasswordField = new CustomRequiredPasswordTextField(OLDPASSWORD_FIELD_ID,
                    oldpassword = new Model<String>());
            oldpasswordField.setRequiredMessageKey("errorMissingOldPassword");
            add(oldpasswordField);
            add(new ErrorComponentFeedbackLabel("oldpassword_feedback", oldpasswordField));
            focus(oldpasswordField);

            final CustomRequiredPasswordTextField password1Field = new CustomRequiredPasswordTextField(PASSWORD1_FIELD_ID,
                    password1 = new Model<String>());
            password1Field.setRequiredMessageKey("errorMissingNewPassword");
            add(password1Field);
            add(new ErrorComponentFeedbackLabel("password1_feedback", password1Field));

            final CustomRequiredPasswordTextField password2Field = new CustomRequiredPasswordTextField(PASSWORD2_FIELD_ID,
                    password2 = new Model<String>());
            password2Field.setRequiredMessageKey("errorMissingNewRepeatPassword");
            add(password2Field);
            add(new ErrorComponentFeedbackLabel("password2_feedback", password2Field));

            add(new EqualPasswordInputValidator(password1Field, password2Field));

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("update password for " + protocolContext.getSubject());

                    try {
                        passwordDeviceService.update(protocolContext.getSubject(), oldpassword.getObject(), password1.getObject());

                        protocolContext.setSuccess(true);
                        exit();
                    }

                    catch (SubjectNotFoundException e) {
                        password1Field.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(), "update: subject not found",
                                LogLevelType.ERROR);
                    } catch (DeviceAuthenticationException e) {
                        oldpasswordField.error(getLocalizer().getString("errorOldPasswordNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(), "register: device not found",
                                LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        password1Field.error(getLocalizer().getString("errorOldPasswordNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(), "update: device not found",
                                LogLevelType.ERROR);
                    } catch (DeviceDisabledException e) {
                        password1Field.error(getLocalizer().getString("errorDeviceDisabled", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(), "update: device disabled",
                                LogLevelType.ERROR);
                    }
                }
            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    protocolContext.setSuccess(false);
                    exit();
                }

            };
            cancel.setDefaultFormProcessing(false);
            add(cancel);

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }


    public void exit() {

        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
