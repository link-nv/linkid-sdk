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
import net.link.safeonline.webapp.components.CustomRequiredPasswordTextField;
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
import org.apache.wicket.model.Model;


public class UpdatePage extends TemplatePage implements IHeaderContributor {

    static final Log           LOG                      = LogFactory.getLog(UpdatePage.class);

    private static final long  serialVersionUID         = 1L;

    public static final String UPDATE_FORM_ID           = "update_form";
    public static final String OLD_PIN_FIELD_ID         = "oldPin";
    public static final String NEW_PIN_FIELD_ID         = "newPin";
    public static final String NEW_PIN_CONFIRM_FIELD_ID = "newPinConfirm";
    public static final String UPDATE_BUTTON_ID         = "update";
    public static final String CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = OptionDeviceService.JNDI_BINDING)
    OptionDeviceService        optionDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;


    public UpdatePage() {

        getHeader();
        getSidebar(localize("helpOptionUpdate"));
        getContent().add(new UpdateForm(UPDATE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    public void renderHead(IHeaderResponse response) {

        response.renderJavascriptReference(new ResourceReference(MainPage.class, "jquery.js"));
        response.renderJavascriptReference(new ResourceReference(MainPage.class, "progress.js"));
        response.renderOnDomReadyJavascript("$('#progressform #update').click(startProgress);");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("optionUpdate");
    }


    class UpdateForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             oldPin;
        Model<String>             newPin;
        Model<String>             newPinConfirm;


        @SuppressWarnings("unchecked")
        public UpdateForm(String id) {

            super(id);
            setMarkupId("progressform");
            setOutputMarkupId(true);

            CustomRequiredPasswordTextField oldPinField = new CustomRequiredPasswordTextField(OLD_PIN_FIELD_ID,
                    oldPin = new Model<String>());
            oldPinField.setRequiredMessageKey("errorMissingOldPIN");

            CustomRequiredPasswordTextField newPinField = new CustomRequiredPasswordTextField(NEW_PIN_FIELD_ID,
                    newPin = new Model<String>());
            newPinField.setRequiredMessageKey("errorMissingNewPIN");

            CustomRequiredPasswordTextField newPinConfirmField = new CustomRequiredPasswordTextField(NEW_PIN_CONFIRM_FIELD_ID,
                    newPinConfirm = new Model<String>());
            newPinConfirmField.setRequiredMessageKey("errorMissingRepeatNewPIN");

            Button updateButton = new Button(UPDATE_BUTTON_ID);
            updateButton.setMarkupId("update");
            updateButton.setOutputMarkupId(true);

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
            add(new ErrorComponentFeedbackLabel("oldPin_feedback", oldPinField));
            add(new ErrorComponentFeedbackLabel("newPin_feedback", newPinField));
            add(new ErrorComponentFeedbackLabel("newPinConfirm_feedback", newPinConfirmField));
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
            focus(oldPinField);
        }

        @Override
        public void onSubmit() {

            if (!newPin.getObject().equals(newPinConfirm.getObject())) {
                UpdateForm.this.error(localize("errorUnmatchedPin"));
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
                UpdateForm.this.error(localize("optionAuthenticationFailed"));
                HelpdeskLogger.add(localize("update: %s", e.getMessage()), //
                        LogLevelType.ERROR);
                LOG.error("update failed", e);
                exit(false);
            }

        }
    }


    public void exit(boolean success) {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        protocolContext.setSuccess(success);

        throw new RedirectToUrlException("deviceexit");
    }
}
