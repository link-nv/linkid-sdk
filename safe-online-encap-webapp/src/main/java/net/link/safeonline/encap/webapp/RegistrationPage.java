/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceRegistrationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.webapp.AuthenticationPage.Goal;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.webapp.components.CustomRequiredTextField;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;


public class RegistrationPage extends TemplatePage {

    static final Log           LOG                 = LogFactory.getLog(RegistrationPage.class);

    private static final long  serialVersionUID    = 1L;

    public static final String REGISTER_FORM_ID    = "register_form";
    public static final String MOBILE_FIELD_ID     = "mobile";
    public static final String ACTIVATION_FIELD_ID = "activation";
    public static final String ACTIVATE_BUTTON_ID  = "activate";
    public static final String REGISTER_BUTTON_ID  = "register";
    public static final String CANCEL_BUTTON_ID    = "cancel";

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;


    public RegistrationPage() {

        getHeader();
        getSidebar(localize("helpRegisterMobile"));
        getContent().add(new RegisterForm(REGISTER_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("mobileRegister");
    }


    class RegisterForm extends Form<String> {

        private static final long            serialVersionUID = 1L;

        Model<PhoneNumber>                   mobile;
        Model<String>                        activation;

        CustomRequiredTextField<PhoneNumber> mobileField;
        TextField<String>                    activationField;

        private Button                       activateButton;
        private Button                       registerButton;
        private Button                       cancelButton;


        @SuppressWarnings("unchecked")
        public RegisterForm(String id) {

            super(id);

            mobileField = new CustomRequiredTextField<PhoneNumber>(MOBILE_FIELD_ID, mobile = new Model<PhoneNumber>(), PhoneNumber.class);
            mobileField.setRequired(true);
            mobileField.setRequiredMessageKey("errorMissingMobileNumber");

            activationField = new TextField(ACTIVATION_FIELD_ID, activation = new Model<String>());
            activationField.setEnabled(false);

            activateButton = new Button(ACTIVATE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("register mobile: " + mobile.getObject().getNumber());

                    try {
                        EncapDeviceService encapDeviceService = EjbUtils.getEJB(EncapDeviceService.JNDI_BINDING, EncapDeviceService.class);

                        activation.setObject(encapDeviceService.register(mobile.getObject().getNumber()));
                        EncapSession.get().setDeviceBean(encapDeviceService);
                    }

                    catch (DeviceRegistrationException e) {
                        RegisterForm.this.error(localize("mobileRegistrationFailed"));
                        HelpdeskLogger.add(localize("requestActivation: %s", e.getMessage()), //
                                LogLevelType.ERROR);
                        LOG.error("reg failed", e);
                    } catch (MobileException e) {
                        RegisterForm.this.error(localize("mobileCommunicationFailed"));
                        HelpdeskLogger.add(localize("requestActivation: %s", e.getMessage()), //
                                LogLevelType.ERROR);
                        LOG.error("conn failed", e);
                    }
                }
            };

            registerButton = new Button(REGISTER_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onSubmit() {

                    throw new RestartResponseException(new AuthenticationPage(Goal.REGISTER_DEVICE, mobile.getObject().getNumber()));
                }
            };

            cancelButton = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
                    protocolContext.setSuccess(false);
                    protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

                    throw new RedirectToUrlException("deviceexit");
                }

            };
            cancelButton.setDefaultFormProcessing(false);

            // Add em to the page.
            add(mobileField, activationField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField));
            add(activateButton, registerButton, cancelButton);
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
            focus(mobileField);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            activationField.setVisible(activation.getObject() != null);
            mobileField.setEnabled(activation.getObject() == null);

            activateButton.setVisible(activation.getObject() == null);
            registerButton.setVisible(activation.getObject() != null);

            super.onBeforeRender();
        }

        protected String getUserId()
                throws SubjectNotFoundException, PermissionDeniedException {

            try {
                return getNameIdentifierMappingClient(SafeOnlineNodeKeyStore.class).getUserId(mobile.getObject().getNumber());
            } catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
                LOG.error("subject not found: " + mobile);
                throw new SubjectNotFoundException();
            } catch (RequestDeniedException e) {
                LOG.error("request denied: " + e.getMessage());
                throw new PermissionDeniedException(e.getMessage());
            } catch (WSClientTransportException e) {
                LOG.error("failed to contact web service: " + e.getMessage());
                throw new PermissionDeniedException(e.getMessage());
            }
        }
    }
}
