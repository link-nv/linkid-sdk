/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.Model;


public class RegistrationPage extends TemplatePage implements IHeaderContributor {

    static final Log           LOG                  = LogFactory.getLog(RegistrationPage.class);

    private static final long  serialVersionUID     = 1L;

    public static final String REGISTER_FORM_ID     = "register_form";
    public static final String PIN_FIELD_ID         = "pin";
    public static final String PIN_CONFIRM_FIELD_ID = "pinConfirm";
    public static final String REGISTER_BUTTON_ID   = "register";
    public static final String CANCEL_BUTTON_ID     = "cancel";

    @EJB(mappedName = OptionDeviceService.JNDI_BINDING)
    OptionDeviceService        optionDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;


    public RegistrationPage() {

        getHeader();
        getSidebar(localize("helpOptionRegistration"));
        getContent().add(new RegisterForm(REGISTER_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void renderHead(IHeaderResponse response) {

        response.renderJavascriptReference(new ResourceReference(MainPage.class, "jquery.js"));
        response.renderJavascriptReference(new ResourceReference(MainPage.class, "progress.js"));
        response.renderOnDomReadyJavascript("$('#progressform #register').click(startProgress);");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("optionRegister");
    }


    class RegisterForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             pin;
        Model<String>             pinConfirm;


        @SuppressWarnings("unchecked")
        public RegisterForm(String id) {

            super(id);
            setMarkupId("progressform");
            setOutputMarkupId(true);

            PasswordTextField pinField = new PasswordTextField(PIN_FIELD_ID, pin = new Model<String>());
            pinField.setRequired(true);

            PasswordTextField pinConfirmField = new PasswordTextField(PIN_CONFIRM_FIELD_ID, pinConfirm = new Model<String>());
            pinConfirmField.setRequired(true);

            Button registerButton = new Button(REGISTER_BUTTON_ID);
            registerButton.setMarkupId("register");
            registerButton.setOutputMarkupId(true);

            Button cancelButton = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit(false);
                }

            };
            cancelButton.setDefaultFormProcessing(false);

            // Add em to the page.
            add(pinField, pinConfirmField, registerButton, cancelButton);
            add(new ErrorComponentFeedbackLabel("pin_feedback", pinField, new Model<String>(localize("errorMissingChoosePIN"))));
            add(new ErrorComponentFeedbackLabel("pinConfirm_feedback", pinConfirmField, new Model<String>(
                    localize("errorMissingRepeatChoosePIN"))));
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
            focus(pinField);
        }

        @Override
        public void onSubmit() {

            if (!pin.getObject().equals(pinConfirm.getObject())) {
                RegisterForm.this.error(localize("errorUnmatchedPin"));
                HelpdeskLogger.add("register: PINs don't match.", //
                        LogLevelType.ERROR);
                LOG.error("reg failed");

                pin.setObject(null);
                pinConfirm.setObject(null);

                return;
            }

            String imei = OptionDevice.register(pin.getObject());
            try {
                ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
                optionDeviceService.register(protocolContext.getNodeName(), protocolContext.getSubject(), imei);
            } catch (NodeNotFoundException e) {
                RegisterForm.this.error(localize("errorNodeNotFound"));
                HelpdeskLogger.add(localize("node not found for %s", imei), //
                        LogLevelType.ERROR);
            }

            exit(true);
        }
    }


    public void exit(boolean success) {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        protocolContext.setSuccess(success);

        throw new RedirectToUrlException("deviceexit");
    }
}
