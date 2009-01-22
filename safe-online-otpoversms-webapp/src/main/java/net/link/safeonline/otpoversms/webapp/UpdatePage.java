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
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;


public class UpdatePage extends TemplatePage {

    private static final long         serialVersionUID         = 1L;

    public static final String        REQUEST_OTP_FORM_ID      = "request_otp_form";
    public static final String        MOBILE_FIELD_ID          = "mobile";
    public static final String        REQUEST_OTP_BUTTON_ID    = "request_otp";
    public static final String        REQUEST_CANCEL_BUTTON_ID = "request_cancel";

    public static final String        UPDATE_FORM_ID           = "update_form";
    public static final String        OTP_FIELD_ID             = "otp";
    public static final String        OLDPIN_FIELD_ID          = "oldpin";
    public static final String        PIN1_FIELD_ID            = "pin1";
    public static final String        PIN2_FIELD_ID            = "pin2";
    public static final String        SAVE_BUTTON_ID           = "save";
    public static final String        CANCEL_BUTTON_ID         = "cancel";

    @EJB(mappedName = OtpOverSmsDeviceService.JNDI_BINDING)
    transient OtpOverSmsDeviceService otpOverSmsDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService    samlAuthorityService;

    ProtocolContext                   protocolContext;

    boolean                           requested                = false;


    public UpdatePage() {

        super();

        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        getHeader();
        getSidebar(localize("helpOtpOverSmsPinChange"));

        getContent().add(new RequestOtpForm(REQUEST_OTP_FORM_ID));
        getContent().add(new UpdateForm(UPDATE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("deviceUpdate");
    }


    class RequestOtpForm extends Form<String> {

        private static final long serialVersionUID = 1L;
        TextField<PhoneNumber>    mobileField;


        public RequestOtpForm(String id) {

            super(id);

            mobileField = new TextField<PhoneNumber>(MOBILE_FIELD_ID, new Model<PhoneNumber>(
                    new PhoneNumber(protocolContext.getAttribute())), PhoneNumber.class);
            mobileField.setEnabled(false);
            mobileField.setRequired(true);
            add(mobileField);
            add(new ErrorComponentFeedbackLabel("mobile_feedback", mobileField));

            add(new Button(REQUEST_OTP_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("check mobile: " + protocolContext.getAttribute());
                    try {
                        otpOverSmsDeviceService.checkMobile(protocolContext.getAttribute());
                    } catch (SubjectNotFoundException e) {
                        mobileField.error(getLocalizer().getString("mobileNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: subject not found for "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (DeviceDisabledException e) {
                        mobileField.error(getLocalizer().getString("mobileDisabled", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: mobile " + protocolContext.getAttribute()
                                + " disabled", LogLevelType.ERROR);
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute type not found for "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (AttributeNotFoundException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorAttributeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute not found for "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    }

                    LOG.debug("request OTP for mobile: " + protocolContext.getAttribute());
                    try {
                        otpOverSmsDeviceService.requestOtp(WicketUtil.getHttpSession(getRequest()), protocolContext.getAttribute());
                    } catch (ConnectException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: failed to send otp to "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (SafeOnlineResourceException e) {
                        RequestOtpForm.this.error(getLocalizer().getString("errorServiceConnection", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: failed to send otp to "
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
        protected void onBeforeRender() {

            focus(mobileField);

            super.onBeforeRender();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return !requested;
        }
    }

    class UpdateForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             otp;

        Model<String>             oldPin;

        Model<String>             pin1;

        Model<String>             pin2;

        TextField<String>         otpField;


        @SuppressWarnings("unchecked")
        public UpdateForm(String id) {

            super(id);

            otpField = new TextField<String>(OTP_FIELD_ID, otp = new Model<String>());
            otpField.setRequired(true);
            add(otpField);
            add(new ErrorComponentFeedbackLabel("otp_feedback", otpField));

            final PasswordTextField oldpinField = new PasswordTextField(OLDPIN_FIELD_ID, oldPin = new Model<String>());
            add(oldpinField);
            add(new ErrorComponentFeedbackLabel("oldpin_feedback", oldpinField));

            final PasswordTextField password1Field = new PasswordTextField(PIN1_FIELD_ID, pin1 = new Model<String>());
            add(password1Field);
            add(new ErrorComponentFeedbackLabel("pin1_feedback", password1Field));

            final PasswordTextField password2Field = new PasswordTextField(PIN2_FIELD_ID, pin2 = new Model<String>());
            add(password2Field);
            add(new ErrorComponentFeedbackLabel("pin2_feedback", password2Field));

            add(new EqualPasswordInputValidator(password1Field, password2Field));

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    boolean verified;
                    try {
                        verified = otpOverSmsDeviceService.verifyOtp(WicketUtil.getHttpSession(getRequest()),
                                protocolContext.getAttribute(), otp.getObject());
                    } catch (SubjectNotFoundException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "subject not found: "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute type not found for "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (AttributeNotFoundException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorAttributeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute not found for "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (DeviceDisabledException e) {
                        UpdateForm.this.error(getLocalizer().getString("mobileDisabled", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: mobile " + protocolContext.getAttribute()
                                + " disabled", LogLevelType.ERROR);
                        return;
                    }
                    if (!verified) {
                        otpField.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(),
                                "mobile otp: verification failed for mobile " + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    }

                    LOG.debug("update pin for " + protocolContext.getSubject() + " for mobile " + protocolContext.getAttribute());

                    boolean result;
                    try {
                        result = otpOverSmsDeviceService.update(protocolContext.getSubject(), protocolContext.getAttribute(),
                                oldPin.getObject(), pin1.getObject());
                    } catch (SubjectNotFoundException e) {
                        password1Field.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "update: subject not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        password1Field.error(getLocalizer().getString("errorDeviceNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "update: device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceDisabledException e) {
                        password1Field.error(getLocalizer().getString("mobileDisabled", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: mobile " + protocolContext.getAttribute()
                                + " disabled", LogLevelType.ERROR);
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute type not found for "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    } catch (AttributeNotFoundException e) {
                        UpdateForm.this.error(getLocalizer().getString("errorAttributeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.getHttpSession(getRequest()), "login: attribute not found for "
                                + protocolContext.getAttribute(), LogLevelType.ERROR);
                        return;
                    }

                    if (false == result) {
                        oldpinField.error(getLocalizer().getString("errorPinNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "update: device not found",
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
        protected void onBeforeRender() {

            focus(otpField);

            super.onBeforeRender();
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
