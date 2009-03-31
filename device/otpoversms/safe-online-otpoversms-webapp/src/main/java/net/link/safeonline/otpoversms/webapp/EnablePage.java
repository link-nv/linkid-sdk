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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;


public class EnablePage extends TemplatePage {

    static final Log           LOG                      = LogFactory.getLog(EnablePage.class);

    private static final long  serialVersionUID         = 1L;

    public static final String REQUEST_OTP_FORM_ID      = "request_otp_form";
    public static final String MOBILE_FIELD_ID          = "mobile";
    public static final String REQUEST_OTP_BUTTON_ID    = "request_otp";
    public static final String REQUEST_CANCEL_BUTTON_ID = "request_cancel";

    public static final String ENABLE_FORM_ID           = "enable_form";
    public static final String OTP_FIELD_ID             = "otp";
    public static final String PIN_FIELD_ID             = "pin";
    public static final String ENABLE_BUTTON_ID         = "enable";
    public static final String CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;

    private RequestOtpForm     requestForm;

    private EnableForm         enableForm;


    public EnablePage() {

        super();

        getHeader();
        getSidebar(localize("helpOtpOverSmsEnable"));

        getContent().add(requestForm = new RequestOtpForm(REQUEST_OTP_FORM_ID));
        getContent().add(enableForm = new EnableForm(ENABLE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        boolean challenged = OtpOverSmsSession.get().isChallenged();
        requestForm.setVisible(!challenged);
        enableForm.setVisible(challenged);

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("otpOverSmsEnable");
    }


    class RequestOtpForm extends Form<String> {

        private static final long serialVersionUID = 1L;
        TextField<PhoneNumber>    mobileField;
        private Button            requestOtpButton;


        public RequestOtpForm(String id) {

            super(id);

            mobileField = new TextField<PhoneNumber>(MOBILE_FIELD_ID, new Model<PhoneNumber>(new PhoneNumber(
                    ProtocolContext.getProtocolContext(WicketUtil.getHttpSession()).getAttribute())), PhoneNumber.class);
            mobileField.setEnabled(false);
            add(mobileField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField, new Model<String>(localize("errorMissingMobileNumber"))));

            add(requestOtpButton = new Button(REQUEST_OTP_BUTTON_ID) {

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
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "enable: failed to send otp to "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    } catch (SafeOnlineResourceException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "enable: failed to send otp to "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    } catch (SubjectNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "enable: mobile has no registered subject: "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(), "enable: mobile isn't registered: "
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

            focus(requestOtpButton);

            super.onBeforeRender();
        }
    }

    class EnableForm extends Form<String> {

        private static final long       serialVersionUID = 1L;

        Model<String>                   otp;

        Model<String>                   pin;

        CustomRequiredTextField<String> otpField;


        @SuppressWarnings("unchecked")
        public EnableForm(String id) {

            super(id);

            otpField = new CustomRequiredTextField<String>(OTP_FIELD_ID, otp = new Model<String>());
            otpField.setRequired(true);
            otpField.setRequiredMessageKey("errorMissingMobileOTP");
            add(otpField);
            add(new ErrorComponentFeedbackLabel("otp_feedback", otpField));

            final CustomRequiredPasswordTextField pinField = new CustomRequiredPasswordTextField(PIN_FIELD_ID, pin = new Model<String>());
            pinField.setRequiredMessageKey("errorMissingMobilePIN");
            add(pinField);
            add(new ErrorComponentFeedbackLabel("pin_feedback", pinField, new Model<String>(localize("errorMissingMobilePIN"))));

            add(new Button(ENABLE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
                    LOG.debug("enable mobile " + protocolContext.getAttribute() + " for " + protocolContext.getSubject());

                    OtpOverSmsDeviceService otpOverSmsDeviceService = OtpOverSmsSession.get().getDeviceService();

                    try {
                        otpOverSmsDeviceService.enable(protocolContext.getSubject(), pin.getObject(), otp.getObject());

                        protocolContext.setSuccess(true);
                        exit();
                    }

                    catch (DeviceAuthenticationException e) {
                        otpField.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add("mobile otp: verification failed for mobile " + protocolContext.getAttribute(),
                                LogLevelType.ERROR);
                    } catch (SubjectNotFoundException e) {
                        EnableForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add("enable: subject not found", LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        EnableForm.this.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                        HelpdeskLogger.add("enable: device registration not found", LogLevelType.ERROR);
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
