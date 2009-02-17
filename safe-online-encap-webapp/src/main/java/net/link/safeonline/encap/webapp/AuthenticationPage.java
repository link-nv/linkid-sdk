/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class AuthenticationPage extends TemplatePage {

    private static final long      serialVersionUID       = 1L;

    public static final String     AUTHENTICATION_FORM_ID = "authentication_form";
    public static final String     MOBILE_FIELD_ID        = "mobile";
    public static final String     OTP_FIELD_ID           = "otp";
    public static final String     CHALLENGE_BUTTON_ID    = "challenge";
    public static final String     LOGIN_BUTTON_ID        = "login";
    public static final String     CANCEL_BUTTON_ID       = "cancel";

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;

    Goal                           goal;
    ProtocolContext                protocolContext;
    AuthenticationContext          authenticationContext;

    private String                 pageTitle;


    public enum Goal {
        /** Authenticate a user with the device. */
        AUTHENTICATE,
        /** Re-enable a disabled device. */
        ENABLE_DEVICE,
        /** Finalize a device registration. */
        REGISTER_DEVICE
    }


    /**
     * By default, use the {@link Goal#AUTHENTICATE}.
     */
    public AuthenticationPage() {

        this(Goal.AUTHENTICATE);
    }

    /**
     * @see Goal
     */
    public AuthenticationPage(final Goal goal) {

        this.goal = goal;
        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
        authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.toServletRequest(getRequest()).getSession());

        // Header & Sidebar.
        getHeader();
        getSidebar(localize("helpMobileAuthentication")).add(new Link<String>("tryAnotherDevice") {

            private static final long serialVersionUID = 1L;

            {
                setVisible(goal.equals(Goal.AUTHENTICATE));
            }


            @Override
            public void onClick() {

                authenticationContext.setUsedDevice(EncapConstants.ENCAP_DEVICE_ID);
                exit(false);
            }
        });

        // Our content.
        String title = null;
        switch (goal) {
            case AUTHENTICATE:
                pageTitle = localize("%l", "mobileAuthentication");
                title = localize("%l %s", "authenticatingFor", authenticationContext.getApplication());
            break;

            case ENABLE_DEVICE:
                pageTitle = localize("%l", "mobileEnable");
                title = localize("%l %s", "mobile", protocolContext.getAttribute());
            break;

            case REGISTER_DEVICE:
                pageTitle = localize("%l", "registerANewDevice");
            break;
        }
        updatePageTitle();
        Label titleLabel = new Label("title", title);
        if (title == null) {
            titleLabel.setVisible(false);
        }

        ProgressAuthenticationPanel progress = new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate);
        progress.setVisible(goal.equals(Goal.AUTHENTICATE));

        getContent().add(progress);
        getContent().add(new AuthenticationForm(AUTHENTICATION_FORM_ID));
        getContent().add(titleLabel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return pageTitle;
    }


    class AuthenticationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             mobile;
        Model<String>             otp;

        private TextField<String> mobileField;
        private TextField<String> otpField;

        private Button            challengeButton;
        private Button            loginButton;
        private Button            cancelButton;


        @SuppressWarnings("unchecked")
        public AuthenticationForm(String id) {

            super(id);

            // Create our form's components.
            mobileField = new TextField<String>(MOBILE_FIELD_ID, mobile = new Model<String>());
            mobileField.setRequired(true);

            otpField = new TextField<String>(OTP_FIELD_ID, otp = new Model<String>());

            challengeButton = new Button(CHALLENGE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("challenge request for: " + mobile.getObject());
                    try {
                        EncapDeviceService encapDeviceService = EjbUtils.getEJB(EncapDeviceService.JNDI_BINDING, EncapDeviceService.class);

                        encapDeviceService.requestOTP(mobile.getObject());
                        EncapSession.get().setDeviceBean(encapDeviceService);
                    }

                    catch (MobileException e) {
                        AuthenticationForm.this.error(localize("mobileCommunicationFailed"));
                        HelpdeskLogger.add(localize("requestOtp: %s for mobile %s", e.getMessage(), mobile), //
                                LogLevelType.ERROR);
                    }
                }
            };

            loginButton = new Button(LOGIN_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("mobile: " + mobile);
                    HelpdeskLogger.add("login: begin for: " + mobile.getObject(), LogLevelType.INFO);

                    try {
                        EncapDeviceService encapDeviceService = EncapSession.get().getDeviceService();

                        switch (goal) {
                            case AUTHENTICATE:
                                String userId = encapDeviceService.authenticate(mobile.getObject(), otp.getObject());
                                if (null == userId)
                                    // Authentication failed.
                                    throw new DeviceAuthenticationException();

                                // Authentication passed, log the user in.
                                authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
                                authenticationContext.setUsedDevice(EncapConstants.ENCAP_DEVICE_ID);
                                authenticationContext.setIssuer(EncapConstants.ENCAP_DEVICE_ID);
                                authenticationContext.setUserId(userId);

                                exit(true);
                            break;

                            case ENABLE_DEVICE:
                                // Authentication passed, enable the device.
                                encapDeviceService.enable(protocolContext.getSubject(), mobile.getObject(), otp.getObject());
                                protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
                                protocolContext.setSuccess(true);

                                exit(true);
                            break;

                            case REGISTER_DEVICE:
                                // Authentication passed, commit this registration to OLAS.
                                encapDeviceService.commitRegistration(protocolContext.getNodeName(), protocolContext.getSubject(),
                                        mobile.getObject(), otp.getObject());

                                exit(true);
                            break;
                        }

                        // All went well, clear helpdesk events.
                        HelpdeskLogger.clear(WicketUtil.toServletRequest(getRequest()).getSession());
                    }

                    catch (MobileException e) {
                        AuthenticationForm.this.error(localize("mobileCommunicationFailed"));
                        HelpdeskLogger.add(localize("comm: %s for %s", e.getMessage(), mobile.getObject()), //
                                LogLevelType.ERROR);
                    } catch (DeviceDisabledException e) {
                        AuthenticationForm.this.error(localize("errorDeviceDisabled"));
                        HelpdeskLogger.add(localize("device is disabled: %s", mobile.getObject()), //
                                LogLevelType.ERROR);
                    } catch (DeviceAuthenticationException e) {
                        AuthenticationForm.this.error(localize("authenticationFailedMsg"));
                        HelpdeskLogger.add(localize("authentication failed: %s for %s", e.getMessage(), mobile.getObject()), //
                                LogLevelType.ERROR);
                    } catch (SubjectNotFoundException e) {
                        AuthenticationForm.this.error(localize("errorSubjectNotFound"));
                        HelpdeskLogger.add(localize("subject not found for %s", mobile.getObject()), //
                                LogLevelType.ERROR);
                    } catch (NodeNotFoundException e) {
                        AuthenticationForm.this.error(localize("errorNodeNotFound"));
                        HelpdeskLogger.add(localize("node not found for %s", mobile.getObject()), //
                                LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        AuthenticationForm.this.error(localize("errorDeviceRegistrationNotFound"));
                        HelpdeskLogger.add(localize("device not registered: %s", mobile.getObject()), //
                                LogLevelType.ERROR);
                    }

                    throw new RestartResponseException(new AuthenticationPage(goal));
                }
            };

            cancelButton = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit(false);
                }
            };
            cancelButton.setDefaultFormProcessing(false);

            // Add em to the page.
            add(mobileField, otpField);
            add(challengeButton, loginButton, cancelButton);
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            boolean challenged = EncapSession.get().isChallenged();
            mobileField.setEnabled(!challenged);
            otpField.setVisible(challenged);
            otpField.setRequired(challenged);
            challengeButton.setVisible(!challenged);
            loginButton.setVisible(challenged);

            if (mobileField.isEnabled()) {
                focus(mobileField);
            } else if (otpField.isVisible()) {
                focus(otpField);
            } else if (challengeButton.isVisible()) {
                focus(challengeButton);
            } else {
                focus(loginButton);
            }

            super.onBeforeRender();
        }
    }


    void exit(boolean success) {

        switch (goal) {
            case AUTHENTICATE:
                authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

                throw new RedirectToUrlException("authenticationexit");

            case ENABLE_DEVICE:
                protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
                protocolContext.setSuccess(success);

                throw new RedirectToUrlException("deviceexit");

            case REGISTER_DEVICE:
                protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
                protocolContext.setSuccess(success);

                throw new RedirectToUrlException("deviceexit");
        }
    }
}