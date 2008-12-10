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
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.common.HelpPage;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressRegistrationPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class RegistrationPage extends TemplatePage {

    private static final long         serialVersionUID         = 1L;

    static final Log                  LOG                      = LogFactory.getLog(RegistrationPage.class);

    public static final String        REQUEST_OTP_FORM_ID      = "request_otp_form";
    public static final String        MOBILE_FIELD_ID          = "mobile";
    public static final String        REQUEST_OTP_BUTTON_ID    = "request_otp";
    public static final String        REQUEST_CANCEL_BUTTON_ID = "request_cancel";

    public static final String        VERIFY_OTP_FORM_ID       = "verify_otp_form";
    public static final String        OTP_FIELD_ID             = "otp";
    public static final String        PIN1_FIELD_ID            = "pin1";
    public static final String        PIN2_FIELD_ID            = "pin2";
    public static final String        SAVE_BUTTON_ID           = "save";
    public static final String        CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = OtpOverSmsDeviceService.JNDI_BINDING)
    transient OtpOverSmsDeviceService otpOverSmsDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService    samlAuthorityService;

    ProtocolContext                   protocolContext;

    Model<PhoneNumber>                mobile;


    public RegistrationPage() {

        super();

        this.protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        addHeader(this, false);

        getSidebar().add(new Link<String>("help") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                setResponsePage(new HelpPage(getPage()));

            }

        });

        ProgressRegistrationPanel progress = new ProgressRegistrationPanel("progress", ProgressRegistrationPanel.stage.register);
        progress.setVisible(this.protocolContext.getDeviceOperation().equals(DeviceOperationType.NEW_ACCOUNT_REGISTER));
        getContent().add(progress);

        getContent().add(new RequestOtpForm(REQUEST_OTP_FORM_ID));
        getContent().add(new VerifyOtpForm(VERIFY_OTP_FORM_ID));

    }


    class RequestOtpForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public RequestOtpForm(String id) {

            super(id);

            TextField<PhoneNumber> mobileField = new TextField<PhoneNumber>(MOBILE_FIELD_ID,
                    RegistrationPage.this.mobile = new Model<PhoneNumber>(), PhoneNumber.class);
            mobileField.setRequired(true);
            add(mobileField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField));

            add(new Button(REQUEST_OTP_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("request otp for mobile " + RegistrationPage.this.mobile);

                    try {
                        RegistrationPage.this.otpOverSmsDeviceService.requestOtp(WicketUtil.getHttpSession(getRequest()),
                                RegistrationPage.this.mobile.getObject().getNumber());
                    } catch (ConnectException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: failed to send otp"
                                + RegistrationPage.this.mobile, LogLevelType.ERROR);
                        RegistrationPage.this.mobile.setObject(null);
                        return;
                    }

                }

            });

            Button cancel = new Button(REQUEST_CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    RegistrationPage.this.protocolContext.setSuccess(false);
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

            return null == RegistrationPage.this.mobile.getObject();
        }
    }

    class VerifyOtpForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             otp;

        Model<String>             pin1;

        Model<String>             pin2;


        public VerifyOtpForm(String id) {

            super(id);

            final TextField<String> otpField = new TextField<String>(OTP_FIELD_ID, this.otp = new Model<String>());
            otpField.setRequired(true);
            add(otpField);
            add(new ErrorComponentFeedbackLabel("otp_feedback", otpField));

            final PasswordTextField pin1Field = new PasswordTextField(PIN1_FIELD_ID, this.pin1 = new Model<String>());
            add(pin1Field);
            add(new ErrorComponentFeedbackLabel("pin1_feedback", pin1Field));

            final PasswordTextField pin2Field = new PasswordTextField(PIN2_FIELD_ID, this.pin2 = new Model<String>());
            add(pin2Field);
            add(new ErrorComponentFeedbackLabel("pin2_feedback", pin2Field));

            add(new EqualPasswordInputValidator(pin1Field, pin2Field));

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    try {
                        boolean verified = RegistrationPage.this.otpOverSmsDeviceService.verifyOtp(WicketUtil.getHttpSession(getRequest()),
                                VerifyOtpForm.this.otp.getObject());
                        if (!verified) {
                            otpField.error(getLocalizer().getString("authenticationFailedMsg", this));
                            HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(),
                                    "mobile otp: verification failed for mobile " + RegistrationPage.this.mobile, LogLevelType.ERROR);
                            return;
                        }

                        LOG.debug("register mobile " + RegistrationPage.this.mobile + " for "
                                + RegistrationPage.this.protocolContext.getSubject());

                        RegistrationPage.this.otpOverSmsDeviceService.register(RegistrationPage.this.protocolContext.getSubject(),
                                RegistrationPage.this.mobile.getObject().getNumber(), VerifyOtpForm.this.pin1.getObject());
                    } catch (PermissionDeniedException e) {
                        pin2Field.error(getLocalizer().getString("errorPinNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "register: pin not correct",
                                LogLevelType.ERROR);
                        return;
                    } catch (SubjectNotFoundException e) {
                        VerifyOtpForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "register: subject not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        VerifyOtpForm.this.error(getLocalizer().getString("errorDeviceNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "register: device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        VerifyOtpForm.this.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "register: attribute type not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (AttributeNotFoundException e) {
                        VerifyOtpForm.this.error(getLocalizer().getString("errorAttributeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "register: attribute not found",
                                LogLevelType.ERROR);
                        return;
                    }

                    RegistrationPage.this.protocolContext.setSuccess(true);
                    exit();

                }

            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    RegistrationPage.this.protocolContext.setSuccess(false);
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

            return RegistrationPage.this.mobile.getObject() != null;
        }
    }


    public void exit() {

        this.protocolContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
