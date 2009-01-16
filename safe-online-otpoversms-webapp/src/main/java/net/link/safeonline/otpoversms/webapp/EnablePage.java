/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import java.net.ConnectException;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;


public class EnablePage extends TemplatePage {

    private static final long         serialVersionUID         = 1L;

    public static final String        REQUEST_OTP_FORM_ID      = "request_otp_form";
    public static final String        MOBILE_FIELD_ID          = "mobile";
    public static final String        REQUEST_OTP_BUTTON_ID    = "request_otp";
    public static final String        REQUEST_CANCEL_BUTTON_ID = "request_cancel";

    public static final String        ENABLE_FORM_ID           = "enable_form";
    public static final String        OTP_FIELD_ID             = "otp";
    public static final String        PIN_FIELD_ID             = "pin";
    public static final String        ENABLE_BUTTON_ID         = "enable";
    public static final String        CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = OtpOverSmsDeviceService.JNDI_BINDING)
    transient OtpOverSmsDeviceService otpOverSmsDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService    samlAuthorityService;

    ProtocolContext                   protocolContext;

    boolean                           requested                = false;


    public EnablePage() {

        super();

        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        getHeader();
        getSidebar();

        String title = localize("%l %s", "mobile", protocolContext.getAttribute());
        getContent().add(new Label("title", title));

        getContent().add(new RequestOtpForm(REQUEST_OTP_FORM_ID));
        getContent().add(new EnableForm(ENABLE_FORM_ID));
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


        public RequestOtpForm(String id) {

            super(id);

            final TextField<PhoneNumber> mobileField = new TextField<PhoneNumber>(MOBILE_FIELD_ID, new Model<PhoneNumber>(new PhoneNumber(
                    protocolContext.getAttribute())), PhoneNumber.class);
            mobileField.setEnabled(false);
            mobileField.setRequired(true);
            add(mobileField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField));

            add(new Button(REQUEST_OTP_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("request OTP for mobile: " + protocolContext.getAttribute());
                    try {
                        otpOverSmsDeviceService.requestOtp(WicketUtil.getHttpSession(getRequest()), protocolContext.getAttribute());
                    } catch (ConnectException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "enable: failed to send otp to "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (SafeOnlineResourceException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "enable: failed to send otp to "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    }
                    requested = true;

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

            return !requested;
        }
    }

    class EnableForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             otp;

        Model<String>             pin;


        @SuppressWarnings("unchecked")
        public EnableForm(String id) {

            super(id);

            final TextField<String> otpField = new TextField<String>(OTP_FIELD_ID, otp = new Model<String>());
            otpField.setRequired(true);
            add(otpField);
            add(new ErrorComponentFeedbackLabel("otp_feedback", otpField));

            final PasswordTextField pinField = new PasswordTextField(PIN_FIELD_ID, pin = new Model<String>());
            add(pinField);
            add(new ErrorComponentFeedbackLabel("pin_feedback", pinField));

            add(new Button(ENABLE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    boolean verified = otpOverSmsDeviceService.verifyOtp(WicketUtil.getHttpSession(getRequest()), otp.getObject());
                    if (!verified) {
                        otpField.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(),
                                "mobile otp: verification failed for mobile " + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    }

                    LOG.debug("enable mobile " + protocolContext.getAttribute() + " for " + protocolContext.getSubject());

                    boolean result;
                    try {
                        result = otpOverSmsDeviceService.enable(protocolContext.getSubject(), protocolContext.getAttribute(),
                                pin.getObject());
                    } catch (SubjectNotFoundException e) {
                        pinField.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: subject not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        pinField.error(getLocalizer().getString("errorDeviceNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        pinField.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: attribute type not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceRegistrationNotFoundException e) {
                        pinField.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: device registration not found",
                                LogLevelType.ERROR);
                        return;
                    }

                    if (false == result) {
                        pinField.error(getLocalizer().getString("errorPinNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: pin not correct",
                                LogLevelType.ERROR);
                        return;
                    }

                    protocolContext.setSuccess(true);
                    exit();
                }

            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    protocolContext.setSuccess(false);
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

            return requested;
        }
    }


    public void exit() {

        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
