/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressAuthenticationPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class AuthenticationPage extends TemplatePage implements IHeaderContributor {

    private static final long      serialVersionUID       = 1L;

    public static final String     AUTHENTICATION_FORM_ID = "authentication_form";
    public static final String     PIN_FIELD_ID           = "pin";
    public static final String     LOGIN_BUTTON_ID        = "login";
    public static final String     CANCEL_BUTTON_ID       = "cancel";

    @EJB(mappedName = OptionDeviceService.JNDI_BINDING)
    transient OptionDeviceService  optionDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;

    AuthenticationContext          authenticationContext;


    public AuthenticationPage() {

        authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.toServletRequest(getRequest()).getSession());

        // Header & Sidebar.
        getHeader();
        getSidebar(localize("helpOptionAuthentication")).add(new Link<String>("tryAnotherDevice") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                authenticationContext.setUsedDevice(OptionConstants.OPTION_DEVICE_ID);
                exit();
            }
        });

        // Our content.
        ProgressAuthenticationPanel progress = new ProgressAuthenticationPanel("progress", ProgressAuthenticationPanel.stage.authenticate);

        getContent().add(progress);
        getContent().add(new AuthenticationForm(AUTHENTICATION_FORM_ID));
        getContent().add(new Label("title", localize("%l %s", "authenticatingFor", authenticationContext.getApplication())));
    }

    /**
     * {@inheritDoc}
     */
    public void renderHead(IHeaderResponse response) {

        response.renderJavascriptReference(new ResourceReference(MainPage.class, "jquery.js"));
        response.renderJavascriptReference(new ResourceReference(MainPage.class, "progress.js"));
        response.renderOnDomReadyJavascript("$('#progressform #login').click(startProgress);");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("%l", "optionAuthentication");
    }


    class AuthenticationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             pin;
        private PasswordTextField pinField;

        private Button            loginButton;
        private Button            cancelButton;


        @SuppressWarnings("unchecked")
        public AuthenticationForm(String id) {

            super(id);
            setMarkupId("progressform");
            setOutputMarkupId(true);

            // Create our form's components.
            pinField = new PasswordTextField(PIN_FIELD_ID, pin = new Model<String>());

            loginButton = new Button(LOGIN_BUTTON_ID);
            loginButton.setMarkupId("login");
            loginButton.setOutputMarkupId(true);

            cancelButton = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    exit();
                }
            };
            cancelButton.setDefaultFormProcessing(false);

            // Add em to the page.
            add(pinField, loginButton, cancelButton);
            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
            focus(pinField);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onSubmit() {

            try {
                String imei = OptionDevice.validate(pin.getObject());
                String userId = optionDeviceService.authenticate(imei);
                if (null == userId)
                    // Authentication failed.
                    throw new DeviceAuthenticationException();

                // Authentication passed, log the user in.
                authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
                authenticationContext.setUsedDevice(OptionConstants.OPTION_DEVICE_ID);
                authenticationContext.setIssuer(OptionConstants.OPTION_DEVICE_ID);
                authenticationContext.setUserId(userId);

                // All went well, clear helpdesk events & exit successfully.
                HelpdeskLogger.clear(WicketUtil.toServletRequest(getRequest()).getSession());
                exit();
            }

            catch (DeviceAuthenticationException e) {
                LOG.error("authentication failed", e);
                AuthenticationForm.this.error(localize("authenticationFailedMsg"));
                HelpdeskLogger.add(localize("login: failed: %s", e.getMessage()), //
                        LogLevelType.ERROR);
            } catch (DeviceDisabledException e) {
                AuthenticationForm.this.error(localize("errorDeviceDisabled"));
                HelpdeskLogger.add(localize("%s", "login: device is disabled"), //
                        LogLevelType.ERROR);
            } catch (DeviceRegistrationNotFoundException e) {
                AuthenticationForm.this.error(localize("optionNotRegistered"));
                HelpdeskLogger.add(localize("%s", "login: device is not registered"), //
                        LogLevelType.ERROR);
            } catch (DeviceNotFoundException e) {
                AuthenticationForm.this.error(localize("optionNotRegistered"));
                HelpdeskLogger.add(localize("%s", "login: device is not registered"), //
                        LogLevelType.ERROR);
            } catch (SubjectNotFoundException e) {
                AuthenticationForm.this.error(localize("errorSubjectNotFound"));
                HelpdeskLogger.add(localize("%s", "login: subject not found"), //
                        LogLevelType.ERROR);
            }

            throw new RestartResponseException(AuthenticationPage.class);
        }
    }


    void exit() {

        LOG.debug("option: exit");
        authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

        throw new RedirectToUrlException("authenticationexit");
    }
}
