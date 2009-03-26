/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.webapp.components.CustomRequiredPasswordTextField;
import net.link.safeonline.webapp.components.CustomRequiredTextField;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;


public class UpdatePage extends TemplatePage {

    static final Log           LOG                      = LogFactory.getLog(UpdatePage.class);

    private static final long  serialVersionUID         = 1L;

    public static final String REQUEST_OTP_FORM_ID      = "request_otp_form";
    public static final String MOBILE_FIELD_ID          = "mobile";
    public static final String REQUEST_OTP_BUTTON_ID    = "request_otp";
    public static final String REQUEST_CANCEL_BUTTON_ID = "request_cancel";

    public static final String UPDATE_FORM_ID           = "update_form";
    public static final String OTP_FIELD_ID             = "otp";
    public static final String OLDPIN_FIELD_ID          = "oldpin";
    public static final String PIN1_FIELD_ID            = "pin1";
    public static final String PIN2_FIELD_ID            = "pin2";
    public static final String SAVE_BUTTON_ID           = "save";
    public static final String CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;

    private RequestOtpForm     requestForm;

    private UpdateForm         updateForm;


    public UpdatePage() {

        getHeader();
        getSidebar(localize("helpOtpOverSmsPinChange"));

        getContent().add(requestForm = new RequestOtpForm(REQUEST_OTP_FORM_ID));
        getContent().add(updateForm = new UpdateForm(UPDATE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        boolean challenged = OtpOverSmsSession.get().isChallenged();
        requestForm.setVisible(!challenged);
        updateForm.setVisible(challenged);

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("mobileUpdate");
    }


    class RequestOtpForm extends Form<String> {

        private static final long            serialVersionUID = 1L;
        CustomRequiredTextField<PhoneNumber> mobileField;


        public RequestOtpForm(String id) {

            super(id);

            mobileField = new CustomRequiredTextField<PhoneNumber>(MOBILE_FIELD_ID, new Model<PhoneNumber>(new PhoneNumber(
                    ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).getAttribute())), PhoneNumber.class);
            mobileField.setEnabled(false);
            mobileField.setRequired(true);
            mobileField.setRequiredMessageKey("errorMissingMobileNumber");
            add(mobileField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField));

            add(new Button(REQUEST_OTP_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
                    LOG.debug("request OTP for mobile: " + protocolContext.getAttribute());
                    try {
                        OtpOverSmsDeviceService otpOverSmsDeviceService = EjbUtils.getEJB(OtpOverSmsDeviceService.JNDI_BINDING,
                                OtpOverSmsDeviceService.class);

                        otpOverSmsDeviceService.requestOtp(protocolContext.getAttribute());
                        OtpOverSmsSession.get().setDeviceBean(otpOverSmsDeviceService);
                    }

                    catch (SmsServiceException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "update: failed to send otp to "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    } catch (SafeOnlineResourceException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "update: failed to send otp to "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    } catch (SubjectNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "update: mobile has no registered subject: "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "update: mobile isn't registered: "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
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
        protected void onBeforeRender() {

            focus(mobileField);

            super.onBeforeRender();
        }
    }

    class UpdateForm extends Form<String> {

        private static final long       serialVersionUID = 1L;

        Model<String>                   otp;

        Model<String>                   oldPin;

        Model<String>                   pin1;

        Model<String>                   pin2;

        CustomRequiredTextField<String> otpField;


        @SuppressWarnings("unchecked")
        public UpdateForm(String id) {

            super(id);

            otpField = new CustomRequiredTextField<String>(OTP_FIELD_ID, otp = new Model<String>());
            otpField.setRequired(true);
            otpField.setRequiredMessageKey("errorMissingMobileOTP");
            add(otpField);
            add(new ErrorComponentFeedbackLabel("otp_feedback", otpField));

            final CustomRequiredPasswordTextField oldpinField = new CustomRequiredPasswordTextField(OLDPIN_FIELD_ID,
                    oldPin = new Model<String>());
            oldpinField.setRequiredMessageKey("errorMissingOldMobilePIN");
            add(oldpinField);
            add(new ErrorComponentFeedbackLabel("oldpin_feedback", oldpinField));

            final CustomRequiredPasswordTextField password1Field = new CustomRequiredPasswordTextField(PIN1_FIELD_ID,
                    pin1 = new Model<String>());
            password1Field.setRequiredMessageKey("errorMissingNewMobilePIN");
            add(password1Field);
            add(new ErrorComponentFeedbackLabel("pin1_feedback", password1Field));

            final CustomRequiredPasswordTextField password2Field = new CustomRequiredPasswordTextField(PIN2_FIELD_ID,
                    pin2 = new Model<String>());
            password2Field.setRequiredMessageKey("errorMissingRepeatNewMobilePIN");
            add(password2Field);
            add(new ErrorComponentFeedbackLabel("pin2_feedback", password2Field));

            add(new EqualPasswordInputValidator(password1Field, password2Field));

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
                    LOG.debug("update pin for " + protocolContext.getSubject() + " for mobile " + protocolContext.getAttribute());

                    OtpOverSmsDeviceService otpOverSmsDeviceService = OtpOverSmsSession.get().getDeviceService();

                    try {
                        otpOverSmsDeviceService.update(protocolContext.getSubject(), oldPin.getObject(), pin1.getObject(), otp.getObject());

                        protocolContext.setSuccess(true);
                        exit();
                    }

                    catch (SubjectNotFoundException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(), "update: subject not found",
                                LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(), "update: device not registered",
                                LogLevelType.ERROR);
                    } catch (DeviceDisabledException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorDeviceDisabled", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "login: mobile " + protocolContext.getAttribute()
                                + " disabled", LogLevelType.ERROR);
                    } catch (DeviceAuthenticationException e) {
                        oldpinField.error(getLocalizer().getString("errorPinNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest().getSession(), "update: device not found",
                                LogLevelType.ERROR);
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
    }


    public void exit() {

        ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).setValidity(
                samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
