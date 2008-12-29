/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class AuthenticationPage extends TemplatePage {

    private static final long       serialVersionUID       = 1L;

    static final Log                LOG                    = LogFactory.getLog(AuthenticationPage.class);

    public static final String      AUTHENTICATION_FORM_ID = "authentication_form";

    public static final String      LOGIN_NAME_FIELD_ID    = "loginName";

    public static final String      TOKEN_FIELD_ID         = "token";

    public static final String      LOGIN_BUTTON_ID        = "login";

    public static final String      CANCEL_BUTTON_ID       = "cancel";

    @EJB(mappedName = DigipassDeviceService.JNDI_BINDING)
    transient DigipassDeviceService digipassDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService  samlAuthorityService;

    AuthenticationContext           authenticationContext;


    public AuthenticationPage() {

        super();

        authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.toServletRequest(getRequest()).getSession());

        getHeader();

        getSidebar().add(new Link<String>("tryAnotherDevice") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                authenticationContext.setUsedDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
                exit();

            }
        });

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate));

        String title = getLocalizer().getString("digipassAuthentication", this) + " : "
                + getLocalizer().getString("authenticatingFor", this) + " " + authenticationContext.getApplication();
        getContent().add(new Label("title", title));

        getContent().add(new AuthenticationForm(AUTHENTICATION_FORM_ID));

    }


    class AuthenticationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             login;

        Model<String>             token;


        @SuppressWarnings("unchecked")
        public AuthenticationForm(String id) {

            super(id);

            final TextField<String> loginField = new TextField<String>(LOGIN_NAME_FIELD_ID, login = new Model<String>());
            loginField.setRequired(true);
            add(loginField);

            final TextField<String> tokenField = new TextField<String>(TOKEN_FIELD_ID, token = new Model<String>());
            tokenField.setRequired(true);
            add(tokenField);

            add(new Button(LOGIN_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("login: " + login);

                    try {
                        String userId = digipassDeviceService.authenticate(getUserId(), token.getObject());
                        if (null == userId) {
                            AuthenticationForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                            HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "login failed: " + login,
                                    LogLevelType.ERROR);
                            return;
                        }
                        login(userId);
                    } catch (SubjectNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "login: subject not found for " + login,
                                LogLevelType.ERROR);
                        return;
                    } catch (PermissionDeniedException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassAuthenticationFailed", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(),
                                "Failed to contact OLAS to retrieve device mapping for " + login, LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassAuthenticationFailed", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "Digipass Device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceDisabledException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassDisabled", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "Digipass Device is disabled",
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
        authenticationContext.setIssuer(net.link.safeonline.model.digipass.DigipassConstants.DIGIPASS_DEVICE_ID);
        authenticationContext.setUsedDevice(net.link.safeonline.model.digipass.DigipassConstants.DIGIPASS_DEVICE_ID);

        exit();

    }

    public void exit() {

        getResponse().redirect("authenticationexit");
        setRedirect(false);
    }
}