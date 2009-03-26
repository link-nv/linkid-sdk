/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.webapp.components.CustomRequiredPasswordTextField;
import net.link.safeonline.webapp.components.CustomRequiredTextField;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressRegistrationPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;


public class RegistrationPage extends TemplatePage {

    static final Log           LOG                      = LogFactory.getLog(RegistrationPage.class);

    private static final long  serialVersionUID         = 1L;

    public static final String REQUEST_OTP_FORM_ID      = "request_otp_form";
    public static final String MOBILE_FIELD_ID          = "mobile";
    public static final String REQUEST_OTP_BUTTON_ID    = "request_otp";
    public static final String REQUEST_CANCEL_BUTTON_ID = "request_cancel";

    public static final String VERIFY_OTP_FORM_ID       = "verify_otp_form";
    public static final String OTP_FIELD_ID             = "otp";
    public static final String PIN1_FIELD_ID            = "pin1";
    public static final String PIN2_FIELD_ID            = "pin2";
    public static final String SAVE_BUTTON_ID           = "save";
    public static final String CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;

    Model<PhoneNumber>         mobile;


    public RegistrationPage() {

        getHeader();
        getSidebar(localize("helpRegisterOtpOverSms"));

        ProgressRegistrationPanel progress = new ProgressRegistrationPanel("progress", ProgressRegistrationPanel.stage.register);
        progress.setVisible(ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).getDeviceOperation().equals(
                DeviceOperationType.NEW_ACCOUNT_REGISTER));
        getContent().add(progress);

        getContent().add(new RequestOtpForm(REQUEST_OTP_FORM_ID));
        getContent().add(new VerifyOtpForm(VERIFY_OTP_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("registerNewOTPOverSMS");
    }


    class RequestOtpForm extends Form<String> {

        private static final long            serialVersionUID = 1L;
        CustomRequiredTextField<PhoneNumber> mobileField;


        public RequestOtpForm(String id) {

            super(id);

            mobileField = new CustomRequiredTextField<PhoneNumber>(MOBILE_FIELD_ID, mobile = new Model<PhoneNumber>(), PhoneNumber.class);
            mobileField.setRequired(true);
            mobileField.setRequiredMessageKey("errorMissingMobileNumber");
            add(mobileField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField));

            add(new Button(REQUEST_OTP_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
                    LOG.debug("request otp for mobile " + mobile);

                    try {
                        OtpOverSmsDeviceService otpOverSmsDeviceService = EjbUtils.getEJB(OtpOverSmsDeviceService.JNDI_BINDING,
                                OtpOverSmsDeviceService.class);

                        otpOverSmsDeviceService.requestOtp(mobile.getObject().getNumber());
                        OtpOverSmsSession.get().setDeviceBean(otpOverSmsDeviceService);
                    }

                    catch (SmsServiceException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "request: failed to send otp" + mobile.getObject(),
                                LogLevelType.ERROR);
                        mobile.setObject(null);
                        return;
                    } catch (SafeOnlineResourceException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "request: failed to send otp" + mobile.getObject(),
                                LogLevelType.ERROR);
                        mobile.setObject(null);
                        return;
                    } catch (SubjectNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "request: mobile has no registered subject: "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "request: mobile isn't registered: "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    }
                }

            });

            Button cancel = new Button(REQUEST_CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).setSuccess(false);
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
        protected void onBeforeRender() {

            focus(mobileField);

            super.onBeforeRender();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return null == mobile.getObject();
        }
    }

    class VerifyOtpForm extends Form<String> {

        private static final long       serialVersionUID = 1L;

        Model<String>                   otp;

        Model<String>                   pin1;

        Model<String>                   pin2;

        CustomRequiredTextField<String> otpField;


        public VerifyOtpForm(String id) {

            super(id);

            otpField = new CustomRequiredTextField<String>(OTP_FIELD_ID, otp = new Model<String>());
            otpField.setRequired(true);
            otpField.setRequiredMessageKey("errorMissingMobileOTP");
            add(otpField);
            add(new ErrorComponentFeedbackLabel("otp_feedback", otpField));

            final CustomRequiredPasswordTextField pin1Field = new CustomRequiredPasswordTextField(PIN1_FIELD_ID, pin1 = new Model<String>());
            pin1Field.setRequiredMessageKey("errorMissingChooseMobilePIN");
            add(pin1Field);
            add(new ErrorComponentFeedbackLabel("pin1_feedback", pin1Field));

            final CustomRequiredPasswordTextField pin2Field = new CustomRequiredPasswordTextField(PIN2_FIELD_ID, pin2 = new Model<String>());
            pin2Field.setRequiredMessageKey("errorMissingRepeatChooseMobilePIN");
            add(pin2Field);
            add(new ErrorComponentFeedbackLabel("pin2_feedback", pin2Field));

            add(new EqualPasswordInputValidator(pin1Field, pin2Field));

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
                    OtpOverSmsDeviceService otpOverSmsDeviceService = OtpOverSmsSession.get().getDeviceService();

                    try {
                        LOG.debug("register mobile " + mobile + " for " + protocolContext.getSubject());

                        otpOverSmsDeviceService.register(protocolContext.getNodeName(), protocolContext.getSubject(), pin1.getObject(),
                                otp.getObject());

                        protocolContext.setSuccess(true);
                        exit();
                    }

                    catch (DeviceAuthenticationException e) {
                        otpField.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(),
                                "mobile otp: verification failed for mobile " + mobile, LogLevelType.ERROR);
                    }
                }
            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).setSuccess(false);
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
        protected void onBeforeRender() {

            focus(otpField);

            super.onBeforeRender();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return mobile.getObject() != null;
        }
    }


    public void exit() {

        ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).setValidity(
                samlAuthorityService.getAuthnAssertionValidity());
        throw new RedirectToUrlException("deviceexit");
    }
}
