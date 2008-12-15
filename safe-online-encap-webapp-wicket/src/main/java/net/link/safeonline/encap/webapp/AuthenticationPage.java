/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.encap.EncapConstants;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class AuthenticationPage extends TemplatePage {

    private static final long      serialVersionUID       = 1L;

    static final Log               LOG                    = LogFactory.getLog(AuthenticationPage.class);

    public static final String     AUTHENTICATION_FORM_ID = "authentication_form";
    public static final String     MOBILE_FIELD_ID        = "mobile";
    public static final String     OTP_FIELD_ID           = "otp";
    public static final String     LOGIN_BUTTON_ID        = "login";
    public static final String     CANCEL_BUTTON_ID       = "cancel";
    public static final String     CHALLENGE_BUTTON_ID    = "challenge";

    @EJB(mappedName = EncapDeviceService.JNDI_BINDING)
    transient EncapDeviceService   encapDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;

    AuthenticationContext          authenticationContext;


    /**
     * @param enable
     *            <code>false</code>: Authenticating. <code>true</code>: Enabling device.
     */
    public AuthenticationPage(boolean enable) {

        this.authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.toServletRequest(getRequest()).getSession());

        // Header & Sidebar.
        addHeader(this);
        getSidebar().add(new Link<String>("tryAnotherDevice") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                AuthenticationPage.this.authenticationContext.setUsedDevice(EncapConstants.ENCAP_DEVICE_ID);
                exit();
            }
        });

        // Our content.
        String title = String.format("%s: %s %s", getLocalizer().getString("encapAuthentication", this), getLocalizer().getString(
                "authenticatingFor", this), this.authenticationContext.getApplication());
        getContent().add(new Label("title", title));
        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate));
        getContent().add(new AuthenticationForm(AUTHENTICATION_FORM_ID));
    }


    class AuthenticationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        String                    challenge;
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
            this.mobileField = new TextField<String>(MOBILE_FIELD_ID, this.mobile = new Model<String>());
            this.mobileField.setRequired(true);

            this.otpField = new TextField<String>(OTP_FIELD_ID, this.otp = new Model<String>());
            this.otpField.setRequired(true);

            this.challengeButton = new Button(CHALLENGE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    try {
                        // Verify mobile exists in OLAS and is not disabled.
                        AuthenticationPage.this.encapDeviceService.checkMobile(AuthenticationForm.this.mobile.getObject());

                        // Ask Encap to send OTP, we get back a challenge.
                        AuthenticationForm.this.challenge = AuthenticationPage.this.encapDeviceService
                                                                                                      .requestOTP(AuthenticationForm.this.mobile
                                                                                                                                                .getObject());
                    }

                    catch (SubjectNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("mobileNotRegistered", this));
                        HelpdeskLogger.add(String.format("requestOtp: subject not found for %s", AuthenticationForm.this.mobile), //
                                LogLevelType.ERROR);
                    } catch (DeviceDisabledException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("mobileDisabled", this));
                        HelpdeskLogger.add(String.format("requestOtp: mobile %s disabled", AuthenticationForm.this.mobile), //
                                LogLevelType.ERROR);
                    } catch (MobileException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("mobileCommunicationFailed", this));
                        HelpdeskLogger.add(String.format("requestOtp: %s for mobile %s", e.getMessage(), AuthenticationForm.this.mobile), //
                                LogLevelType.ERROR);
                    } catch (AttributeTypeNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(String.format("requestOtp: %s", e.getMessage()), //
                                LogLevelType.ERROR);
                    } catch (AttributeNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("errorAttributeNotFound", this));
                        HelpdeskLogger.add(String.format("requestOtp: %s", e.getMessage()), //
                                LogLevelType.ERROR);
                    }
                }
            };

            this.loginButton = new Button(LOGIN_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("mobile: " + AuthenticationForm.this.mobile);
                    HelpdeskLogger.add("login: begin for: " + AuthenticationForm.this.mobile.getObject(), LogLevelType.INFO);

                    try {
                        String userId = AuthenticationPage.this.encapDeviceService.authenticate(AuthenticationForm.this.mobile.getObject(),
                                AuthenticationForm.this.challenge, AuthenticationForm.this.otp.getObject());
                        if (null == userId)
                            throw new MobileAuthenticationException();

                        login(userId);
                        HelpdeskLogger.clear(WicketUtil.toServletRequest(getRequest()).getSession());
                    }

                    catch (MobileException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("mobileCommunicationFailed", this));
                        HelpdeskLogger.add(String.format("login: comm: %s for %s", e.getMessage(), AuthenticationForm.this.mobile), //
                                LogLevelType.ERROR);
                    } catch (MobileAuthenticationException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(String.format("login: failed: %s for %s", e.getMessage(), AuthenticationForm.this.mobile), //
                                LogLevelType.ERROR);
                    } catch (SubjectNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("mobileNotRegistered", this));
                        HelpdeskLogger.add(String.format("login: subject not found for %s", AuthenticationForm.this.mobile), //
                                LogLevelType.ERROR);
                    }
                }
            };

            this.cancelButton = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit();
                }
            };
            this.cancelButton.setDefaultFormProcessing(false);

            // Add em to the page.
            add(this.mobileField, this.otpField);
            add(this.challengeButton, this.loginButton, this.cancelButton);
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            this.mobileField.setEnabled(this.challenge == null);
            this.otpField.setVisible(this.challenge != null);
            this.challengeButton.setVisible(this.challenge == null);
            this.loginButton.setVisible(this.challenge != null);

            super.onBeforeRender();
        }
    }


    public void login(String userId) {

        this.authenticationContext.setUserId(userId);
        this.authenticationContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
        this.authenticationContext.setIssuer(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);
        this.authenticationContext.setUsedDevice(net.link.safeonline.model.encap.EncapConstants.ENCAP_DEVICE_ID);

        exit();
    }

    public void exit() {

        throw new RedirectToUrlException("authenticationexit");
    }
}
