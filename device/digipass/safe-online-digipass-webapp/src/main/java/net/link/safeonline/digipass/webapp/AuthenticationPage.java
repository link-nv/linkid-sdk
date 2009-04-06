/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.InternalInconsistencyException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.digipass.DigipassConstants;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.OlasServiceFactory;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.webapp.template.SideLink;
import net.link.safeonline.webapp.template.SidebarBorder;
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

    static final Log           LOG                    = LogFactory.getLog(AuthenticationPage.class);

    private static final long  serialVersionUID       = 1L;

    public static final String AUTHENTICATION_FORM_ID = "authentication_form";

    public static final String LOGIN_NAME_FIELD_ID    = "loginName";

    public static final String TOKEN_FIELD_ID         = "token";

    public static final String LOGIN_BUTTON_ID        = "login";

    public static final String CANCEL_BUTTON_ID       = "cancel";

    @EJB(mappedName = DigipassDeviceService.JNDI_BINDING)
    DigipassDeviceService      digipassDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    SamlAuthorityService       samlAuthorityService;

    @EJB(mappedName = NodeAuthenticationService.JNDI_BINDING)
    NodeAuthenticationService  nodeAuthenticationService;


    public AuthenticationPage() {

        super();

        getHeader();
        Link<String> tryAnotherDeviceLink = new Link<String>(SidebarBorder.LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                AuthenticationContext.getAuthenticationContext(WicketUtil.getServletRequest().getSession()).setUsedDevice(
                        DigipassConstants.DIGIPASS_DEVICE_ID);

                exit();
            }
        };
        getSidebar(localize("helpDigipassAuthentication"), new SideLink(tryAnotherDeviceLink, localize("tryAnotherDevice")));

        getContent().add(new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate));

        String title = localize("%l %s", "authenticatingFor", AuthenticationContext.getAuthenticationContext(
                WicketUtil.getServletRequest().getSession()).getApplication());
        getContent().add(new Label("title", title));

        getContent().add(new AuthenticationForm(AUTHENTICATION_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("digipassAuthentication");
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
            focus(loginField);

            final TextField<String> tokenField = new TextField<String>(TOKEN_FIELD_ID, token = new Model<String>());
            tokenField.setRequired(true);
            add(tokenField);

            add(new Button(LOGIN_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("login: " + login);

                    try {
                        String userId = getUserId();

                        digipassDeviceService.authenticate(userId, token.getObject());
                        login(userId);

                        HelpdeskLogger.clear(WicketUtil.getServletRequest().getSession());
                    }

                    catch (DeviceAuthenticationException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(), "login failed: " + login, LogLevelType.ERROR);
                    } catch (DeviceRegistrationNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(), "Digipass device not registered",
                                LogLevelType.ERROR);
                    } catch (SubjectNotFoundException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(), "login: subject not found for " + login,
                                LogLevelType.ERROR);
                    } catch (PermissionDeniedException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassAuthenticationFailed", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(),
                                "Failed to contact OLAS to retrieve device mapping for " + login, LogLevelType.ERROR);
                    } catch (DeviceDisabledException e) {
                        AuthenticationForm.this.error(getLocalizer().getString("digipassDisabled", this));
                        HelpdeskLogger.add(WicketUtil.getServletRequest().getSession(), "Digipass Device is disabled", LogLevelType.ERROR);
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

        protected String getUserId()
                throws SubjectNotFoundException, PermissionDeniedException {

            try {
                return OlasServiceFactory.getIdMappingService(SafeOnlineNodeKeyStore.getPrivateKeyEntry()).getUserId(login.getObject());
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
        }
    }


    public void login(String userId) {

        try {
            AuthenticationContext authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.getServletRequest()
                                                                                                                   .getSession());
            authenticationContext.setIssuer(nodeAuthenticationService.getLocalNode().getName());
            authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
            authenticationContext.setUsedDevice(DigipassConstants.DIGIPASS_DEVICE_ID);
            authenticationContext.setUserId(userId);
        }

        catch (NodeNotFoundException e) {
            throw new InternalInconsistencyException("Couldn't look up local node.");
        }

        exit();
    }

    public void exit() {

        getResponse().redirect("authenticationexit");
        setRedirect(false);
    }
}
