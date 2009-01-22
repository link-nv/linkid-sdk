/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class AuthenticationPage extends TemplatePage {

    private static final long       serialVersionUID       = 1L;

    public static final String      AUTHENTICATION_FORM_ID = "authentication_form";
    public static final String      LOGIN_NAME_FIELD_ID    = "loginName";
    public static final String      PASSWORD_FIELD_ID      = "password";
    public static final String      LOGIN_BUTTON_ID        = "login";
    public static final String      CANCEL_BUTTON_ID       = "cancel";

    @EJB(mappedName = PasswordDeviceService.JNDI_BINDING)
    transient PasswordDeviceService passwordDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService  samlAuthorityService;

    AuthenticationContext           authenticationContext;


    public AuthenticationPage() {

        authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.toServletRequest(getRequest()).getSession());

        getHeader();
        getSidebar(localize("helpPassword")).add(new Link<String>("tryAnotherDevice") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                authenticationContext.setUsedDevice(PasswordConstants.PASSWORD_DEVICE_ID);
                exit();
            }
        });

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate));

        String title = localize("%l %s", "authenticatingFor", authenticationContext.getApplication());
        getContent().add(new Label("title", title));

        getContent().add(new AuthenticationForm(AUTHENTICATION_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("usernamePasswordAuthentication");
    }


    class AuthenticationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             login;

        Model<String>             password;


        @SuppressWarnings("unchecked")
        public AuthenticationForm(String id) {

            super(id);
            setMarkupId(id);

            final TextField<String> loginField = new TextField<String>(LOGIN_NAME_FIELD_ID, login = new Model<String>());
            loginField.setRequired(true);
            add(loginField);
            focus(loginField);

            final PasswordTextField passwordField = new PasswordTextField(PASSWORD_FIELD_ID, password = new Model<String>());

            add(passwordField);

            add(new Button(LOGIN_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("login: " + login);

                    try {
                        String userId = passwordDeviceService.authenticate(getUserId(), password.getObject());
                        if (null == userId) {
                            AuthenticationForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                            HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "login failed: " + login,
                                    LogLevelType.ERROR);
                            return;
                        }
                        login(userId);
                    } catch (SubjectNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "login: subject not found for " + login,
                                LogLevelType.ERROR);
                        return;
                    } catch (PermissionDeniedException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(),
                                "Failed to contact OLAS to retrieve device mapping for " + login, LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "Password Device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceDisabledException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("errorDeviceDisabled", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "Password Device is disabled",
                                LogLevelType.ERROR);
                        return;
                    }
                    HelpdeskLogger.clear(WicketUtil.toServletRequest(getRequest()).getSession());
                    return;
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

        protected String getUserId()
                throws SubjectNotFoundException, PermissionDeniedException {

            AuthIdentityServiceClient authIdentityServiceClient = new AuthIdentityServiceClient();

            NameIdentifierMappingClient idMappingClient = WicketUtil.getOLASIdMappingService(WicketUtil.toServletRequest(getRequest()),
                    authIdentityServiceClient.getPrivateKey(), authIdentityServiceClient.getCertificate());

            String userId;
            try {
                userId = idMappingClient.getUserId(login.getObject());
            } catch (net.link.safeonline.sdk.exception.SubjectNotFoundException e) {
                LOG.error("subject not found: " + login);
                throw new SubjectNotFoundException();
            } catch (RequestDeniedException e) {
                LOG.error("request denied: " + e.getMessage());
                throw new PermissionDeniedException(e.getMessage());
            } catch (WSClientTransportException e) {
                LOG.error("failed to contact web service: " + e.getMessage());
                throw new PermissionDeniedException(e.getMessage());
            }
            return userId;
        }
    }


    public void login(String userId) {

        authenticationContext.setUserId(userId);
        authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        authenticationContext.setIssuer(PasswordConstants.PASSWORD_DEVICE_ID);
        authenticationContext.setUsedDevice(PasswordConstants.PASSWORD_DEVICE_ID);

        exit();

    }

    public void exit() {

        getResponse().redirect("authenticationexit");
        setRedirect(false);
    }
}
