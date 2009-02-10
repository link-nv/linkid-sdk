/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.Model;


public class UpdatePage extends TemplatePage {

    private static final long      serialVersionUID         = 1L;

    public static final String     UPDATE_FORM_ID           = "update_form";
    public static final String     OLD_PIN_FIELD_ID         = "oldPin";
    public static final String     NEW_PIN_FIELD_ID         = "newPin";
    public static final String     NEW_PIN_CONFIRM_FIELD_ID = "newPinConfirm";
    public static final String     UPDATE_BUTTON_ID         = "update";
    public static final String     CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = OptionDeviceService.JNDI_BINDING)
    transient OptionDeviceService  optionDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;

    ProtocolContext                protocolContext;


    public UpdatePage() {

        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        getHeader();
        getSidebar(localize("helpUpdateOption"));
        getContent().add(new RegisterForm(UPDATE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("optionUpdate");
    }


    class RegisterForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             oldPin;
        Model<String>             newPin;
        Model<String>             newPinConfirm;


        @SuppressWarnings("unchecked")
        public RegisterForm(String id) {

            super(id);

            PasswordTextField oldPinField = new PasswordTextField(OLD_PIN_FIELD_ID, oldPin = new Model<String>());
            oldPinField.setRequired(true);

            PasswordTextField newPinField = new PasswordTextField(NEW_PIN_FIELD_ID, newPin = new Model<String>());
            newPinField.setRequired(true);

            PasswordTextField newPinConfirmField = new PasswordTextField(NEW_PIN_CONFIRM_FIELD_ID, newPinConfirm = new Model<String>());
            newPinConfirmField.setRequired(true);

            Button updateButton = new Button(UPDATE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    if (!newPin.getObject().equals(newPinConfirm.getObject())) {
                        RegisterForm.this.error(localize("errorUnmatchedPin"));
                        HelpdeskLogger.add("register: PINs don't match.", //
                                LogLevelType.ERROR);
                        LOG.error("reg failed");

                        newPin.setObject(null);
                        newPinConfirm.setObject(null);

                        return;
                    }

                    try {
                        OptionDevice.update(oldPin.getObject(), newPin.getObject());

                        exit(true);
                    }

                    catch (DeviceAuthenticationException e) {
                        RegisterForm.this.error(localize("optionAuthenticationFailed"));
                        HelpdeskLogger.add(localize("update: %s", e.getMessage()), //
                                LogLevelType.ERROR);
                        LOG.error("update failed", e);
                        exit(false);
                    }

                }
            };

            Button cancelButton = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit(false);
                }

            };
            cancelButton.setDefaultFormProcessing(false);

            // Add em to the page.
            add(oldPinField, newPinField, newPinConfirmField, updateButton, cancelButton);
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
            focus(oldPinField);
        }
    }


    public void exit(boolean success) {

        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        protocolContext.setSuccess(success);

        throw new RedirectToUrlException("deviceexit");
    }
}
