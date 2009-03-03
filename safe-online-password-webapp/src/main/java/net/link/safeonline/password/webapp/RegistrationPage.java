/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressRegistrationPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class RegistrationPage extends TemplatePage {

    private static final long       serialVersionUID           = 1L;

    public static final String      ALREADY_REGISTERED_LINK_ID = "already_registered";

    public static final String      REGISTRATION_FORM_ID       = "registration_form";

    public static final String      PASSWORD1_FIELD_ID         = "password1";

    public static final String      PASSWORD2_FIELD_ID         = "password2";

    public static final String      SAVE_BUTTON_ID             = "save";

    public static final String      CANCEL_BUTTON_ID           = "cancel";

    @EJB(mappedName = PasswordDeviceService.JNDI_BINDING)
    transient PasswordDeviceService passwordDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService  samlAuthorityService;

    ProtocolContext                 protocolContext;

    private Link<String>            alreadyRegistered;

    private RegistrationForm        registrationForm;


    public RegistrationPage() {

        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        getHeader();
        getSidebar(localize("helpRegisterPassword"));

        ProgressRegistrationPanel progress = new ProgressRegistrationPanel("progress", ProgressRegistrationPanel.stage.register);
        progress.setVisible(protocolContext.getDeviceOperation().equals(DeviceOperationType.NEW_ACCOUNT_REGISTER));
        getContent().add(progress);

        getContent().add(alreadyRegistered = new Link<String>(ALREADY_REGISTERED_LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                exit();
            }
        });

        getContent().add(registrationForm = new RegistrationForm(REGISTRATION_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        try {
            boolean passwordExists = passwordDeviceService.isPasswordConfigured(protocolContext.getSubject());

            alreadyRegistered.setVisible(passwordExists);
            registrationForm.setVisible(!passwordExists);
        }

        catch (SubjectNotFoundException e) {
            error(getLocalizer().getString("errorSubjectNotFound", this));

            alreadyRegistered.setVisible(false);
            registrationForm.setVisible(false);
        }

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("usernamePassword");
    }


    class RegistrationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             password1;

        Model<String>             password2;


        @SuppressWarnings("unchecked")
        public RegistrationForm(String id) {

            super(id);

            final PasswordTextField password1Field = new PasswordTextField(PASSWORD1_FIELD_ID, password1 = new Model<String>());

            add(password1Field);
            focus(password1Field);
            add(new ErrorComponentFeedbackLabel("password1_feedback", password1Field));

            final PasswordTextField password2Field = new PasswordTextField(PASSWORD2_FIELD_ID, password2 = new Model<String>());

            add(password2Field);
            add(new ErrorComponentFeedbackLabel("password2_feedback", password2Field));

            add(new EqualPasswordInputValidator(password1Field, password2Field));

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("register password for " + protocolContext.getSubject());

                    try {
                        passwordDeviceService.register(protocolContext.getNodeName(), protocolContext.getSubject(), password1.getObject());
                    } catch (NodeNotFoundException e) {
                        password1Field.error(getLocalizer().getString("errorNodeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "register: node not found",
                                LogLevelType.ERROR);
                        return;
                    }

                    protocolContext.setSuccess(true);
                    exit();
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
