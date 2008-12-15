/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap.webapp;

import javax.ejb.EJB;

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
    public static final String     MOBILE_FIELD_ID        = "loginName";
    public static final String     OTP_FIELD_ID           = "otp";
    public static final String     LOGIN_BUTTON_ID        = "mobile";
    public static final String     CANCEL_BUTTON_ID       = "cancel";
    public static final String     CHALLENGE_FIELD_ID     = "challenge";

    public static final String     CHALLENGE_BUTTON_ID    = "requestChallenge";

    @EJB(mappedName = EncapDeviceService.JNDI_BINDING)
    transient EncapDeviceService   encapDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;

    AuthenticationContext          authenticationContext;


    public AuthenticationPage() {

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


        @SuppressWarnings("unchecked")
        public AuthenticationForm(String id) {

            super(id);

            // Create our form's components.
            final TextField<String> mobileField = new TextField<String>(MOBILE_FIELD_ID, this.mobile = new Model<String>());
            mobileField.setEnabled(this.challenge == null);
            mobileField.setRequired(true);

            final TextField<String> otpField = new TextField<String>(OTP_FIELD_ID, this.otp = new Model<String>());
            otpField.setVisible(this.challenge != null);
            otpField.setRequired(true);

            final Button challengeButton = new Button(CHALLENGE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    try {
                        AuthenticationForm.this.challenge = AuthenticationPage.this.encapDeviceService
                                                                                                      .requestOTP(AuthenticationForm.this.mobile
                                                                                                                                                .getObject());
                    }

                    catch (MobileException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("encapServiceFailed", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "mobile: " + e.getMessage() + " for "
                                + AuthenticationForm.this.mobile, LogLevelType.ERROR);
                    }
                }
            };
            challengeButton.setVisible(this.challenge == null);

            final Button loginButton = new Button(LOGIN_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("mobile: " + AuthenticationForm.this.mobile);

                    try {
                        String userId = AuthenticationPage.this.encapDeviceService.authenticate(AuthenticationForm.this.mobile.getObject(),
                                AuthenticationForm.this.challenge, AuthenticationForm.this.otp.getObject());
                        if (null == userId)
                            throw new MobileAuthenticationException();

                        login(userId);
                        HelpdeskLogger.clear(WicketUtil.toServletRequest(getRequest()).getSession());
                    }

                    catch (MobileException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("encapServiceFailed", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "mobile: " + e.getMessage() + " for "
                                + AuthenticationForm.this.mobile, LogLevelType.ERROR);
                    } catch (MobileAuthenticationException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("encapAuthenticationFailed", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "mobile: " + e.getMessage() + " for "
                                + AuthenticationForm.this.mobile, LogLevelType.ERROR);
                    } catch (SubjectNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("encapNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "mobile: subject not found for "
                                + AuthenticationForm.this.mobile, LogLevelType.ERROR);
                    }
                }
            };
            loginButton.setVisible(this.challenge != null);

            final Button cancelButton = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit();
                }
            };
            cancelButton.setDefaultFormProcessing(false);

            // Add em to the page.
            add(mobileField, otpField);
            add(challengeButton, loginButton, cancelButton);
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
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
