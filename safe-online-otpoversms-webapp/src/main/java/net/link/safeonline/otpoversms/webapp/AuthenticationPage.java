/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import java.net.ConnectException;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.otpoversms.OtpOverSmsConstants;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.common.HelpPage;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;


public class AuthenticationPage extends TemplatePage {

    private static final long         serialVersionUID         = 1L;

    static final Log                  LOG                      = LogFactory.getLog(AuthenticationPage.class);

    public static final String        REQUEST_OTP_FORM_ID      = "request_otp_form";
    public static final String        MOBILE_FIELD_ID          = "mobile";
    public static final String        REQUEST_OTP_BUTTON_ID    = "request_otp";
    public static final String        REQUEST_CANCEL_BUTTON_ID = "request_cancel";

    public static final String        VERIFY_OTP_FORM_ID       = "verify_otp_form";
    public static final String        OTP_FIELD_ID             = "otp";
    public static final String        PIN_FIELD_ID             = "pin";
    public static final String        LOGIN_BUTTON_ID          = "login";
    public static final String        CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = OtpOverSmsDeviceService.JNDI_BINDING)
    transient OtpOverSmsDeviceService otpOverSmsDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService    samlAuthorityService;

    AuthenticationContext             authenticationContext;

    String                            mobile;


    public AuthenticationPage() {

        super();

        this.authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.getHttpSession(getRequest()));

        addHeader(this, false);

        getSidebar().add(new Link<String>("tryAnotherDevice") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                AuthenticationPage.this.authenticationContext.setUsedDevice(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID);
                exit();

            }
        });

        getSidebar().add(new Link<String>("help") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                setResponsePage(new HelpPage(getPage()));

            }

        });

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate));

        String title = getLocalizer().getString("otpOverSmsAuthentication", this) + " : "
                + getLocalizer().getString("authenticatingFor", this) + " " + this.authenticationContext.getApplication();
        getContent().add(new Label("title", title));

        getContent().add(new RequestOtpForm(REQUEST_OTP_FORM_ID));
        getContent().add(new VerifyOtpForm(VERIFY_OTP_FORM_ID));
    }


    class RequestOtpForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public RequestOtpForm(String id) {

            super(id);

            final TextField<String> mobileField = new TextField<String>(MOBILE_FIELD_ID, new PropertyModel<String>(AuthenticationPage.this,
                    "mobile"));
            mobileField.setRequired(true);
            add(mobileField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField));

            add(new Button(REQUEST_OTP_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("check mobile: " + AuthenticationPage.this.mobile);
                    try {
                        AuthenticationPage.this.otpOverSmsDeviceService.checkMobile(AuthenticationPage.this.mobile);
                    } catch (SubjectNotFoundException e) {
                        mobileField.error(getLocalizer().getString("mobileNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: subject not found for "
                                + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                        AuthenticationPage.this.mobile = null;
                        return;
                    } catch (DeviceDisabledException e) {
                        mobileField.error(getLocalizer().getString("mobileDisabled", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: mobile " + AuthenticationPage.this.mobile
                                + " disabled", LogLevelType.ERROR);
                        AuthenticationPage.this.mobile = null;
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute type not found for "
                                + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                        AuthenticationPage.this.mobile = null;
                        return;
                    } catch (AttributeNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorAttributeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute not found for "
                                + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                        AuthenticationPage.this.mobile = null;
                        return;
                    }

                    LOG.debug("request OTP for mobile: " + AuthenticationPage.this.mobile);
                    try {
                        AuthenticationPage.this.otpOverSmsDeviceService.requestOtp(WicketUtil.getHttpSession(getRequest()),
                                AuthenticationPage.this.mobile);
                    } catch (ConnectException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: failed to send otp to "
                                + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                        AuthenticationPage.this.mobile = null;
                        return;
                    }

                }

            });

            Button cancel = new Button(REQUEST_CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit();
                }

            };
            cancel.setDefaultFormProcessing(false);
            add(cancel);

            add(new ErrorFeedbackPanel("request_feedback", new ComponentFeedbackMessageFilter(this)));

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return null == AuthenticationPage.this.mobile;
        }
    }

    class VerifyOtpForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        String                    otp;

        String                    pin;


        public VerifyOtpForm(String id) {

            super(id);

            final TextField<String> otpField = new TextField<String>(OTP_FIELD_ID, new PropertyModel<String>(this, "otp"));
            otpField.setRequired(true);
            add(otpField);
            add(new ErrorComponentFeedbackLabel("otp_feedback", otpField));

            final PasswordTextField pinField = new PasswordTextField(PIN_FIELD_ID, new PropertyModel<String>(this, "pin"));
            add(pinField);
            add(new ErrorComponentFeedbackLabel("pin_feedback", pinField));

            add(new Button(LOGIN_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    boolean verified = AuthenticationPage.this.otpOverSmsDeviceService.verifyOtp(WicketUtil.getHttpSession(getRequest()),
                            VerifyOtpForm.this.otp);
                    if (!verified) {
                        otpField.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(),
                                "mobile otp: verification failed for mobile " + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                        return;
                    }
                    try {
                        String userId = AuthenticationPage.this.otpOverSmsDeviceService.authenticate(AuthenticationPage.this.mobile,
                                VerifyOtpForm.this.pin);
                        if (null == userId) {
                            VerifyOtpForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                            HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "login failed: "
                                    + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                            return;
                        }
                        login(userId);
                    } catch (SubjectNotFoundException e) {
                        VerifyOtpForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "subject not found: "
                                + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        VerifyOtpForm.this.error(getLocalizer().getString("errorDeviceNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "device not found: "
                                + AuthenticationPage.this.mobile, LogLevelType.ERROR);
                        return;
                    }
                }

            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit();
                }

            };
            cancel.setDefaultFormProcessing(false);
            add(cancel);

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return AuthenticationPage.this.mobile != null;
        }
    }


    public void login(String userId) {

        this.authenticationContext.setUserId(userId);
        this.authenticationContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
        this.authenticationContext.setIssuer(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID);
        this.authenticationContext.setUsedDevice(OtpOverSmsConstants.OTPOVERSMS_DEVICE_ID);

        exit();

    }

    public void exit() {

        getResponse().redirect("authenticationexit");
        setRedirect(false);
    }
}
