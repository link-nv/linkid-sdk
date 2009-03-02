/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressRegistrationPanel;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class NewUserPage extends AuthenticationTemplatePage {

    private static final long         serialVersionUID      = 1L;

    public static final String        NEW_USER_FORM_ID      = "new_user_form";
    public static final String        EXISTING_USER_LINK_ID = "existingUser";
    public static final String        LOGIN_NAME_FIELD_ID   = "loginName";
    public static final String        CAPTCHA_FIELD_ID      = "captcha";
    public static final String        CAPTCHA_IMAGE_ID      = "captchaImage";
    public static final String        CAPTCHA_REFRESH_ID    = "captchaRefresh";
    public static final String        REGISTER_BUTTON_ID    = "register";

    @EJB(mappedName = UserRegistrationService.JNDI_BINDING)
    transient UserRegistrationService userRegistrationService;


    public NewUserPage() {

        getSidebar(localize("helpNewUser")).add(new Link<String>(EXISTING_USER_LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RestartResponseException(new MainPage());
            }
        });

        getHeader();

        getContent().add(new ProgressRegistrationPanel("progress", ProgressRegistrationPanel.stage.choose));

        getContent().add(new NewUserForm(NEW_USER_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));
        String title = localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
        return title;
    }


    class NewUserForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             login;

        Model<String>             captcha;


        @SuppressWarnings("unchecked")
        public NewUserForm(String id) {

            super(id);
            setMarkupId(id);

            final TextField<String> loginField = new TextField<String>(LOGIN_NAME_FIELD_ID, login = new Model<String>());
            loginField.setRequired(true);
            add(loginField);
            focus(loginField);
            add(new ErrorComponentFeedbackLabel("login_feedback", loginField));

            final TextField<String> captchaField = new TextField<String>(CAPTCHA_FIELD_ID, captcha = new Model<String>());
            captchaField.setRequired(true);
            add(captchaField);
            add(new ErrorComponentFeedbackLabel("captcha_feedback", captchaField));

            final Image captchaImage = new Image(CAPTCHA_IMAGE_ID, "override");
            captchaImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                    + "/captcha.jpg?cacheid=" + Math.random() * 1000000));
            captchaImage.setOutputMarkupId(true);
            add(captchaImage);

            // TODO: something wrong when trying to refresh for the second time ...
            add(new AjaxLink(CAPTCHA_REFRESH_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick(AjaxRequestTarget target) {

                    target.addComponent(captchaImage);
                }
            });

            add(new Button(REGISTER_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("register: " + login);

                    HelpdeskLogger.add("account creation: login=" + login, LogLevelType.INFO);

                    String validCaptcha = (String) WicketUtil.getHttpSession(getRequest()).getAttribute(
                            com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);

                    LOG.debug("valid captcha: " + validCaptcha);
                    LOG.debug("given captcha: " + captcha.getObject());

                    if (null == validCaptcha) {
                        NewUserForm.this.error(localize("errorNoCaptcha"));
                        return;
                    }

                    if (!validCaptcha.equals(captcha.getObject())) {
                        captchaField.error(localize("errorInvalidCaptcha"));
                        captchaField.setModelObject(null);
                        return;
                    }

                    SubjectEntity subject;
                    try {
                        subject = userRegistrationService.registerUser(login.getObject());
                    } catch (ExistingUserException e) {
                        loginField.error(localize("errorLoginTaken"));
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        loginField.error(localize("errorLoginTaken"));
                        return;
                    } catch (PermissionDeniedException e) {
                        loginField.error(localize("errorPermissionDenied"));
                        return;
                    } catch (AttributeUnavailableException e) {
                        loginField.error(localize("errorLoginTaken"));
                        return;
                    }

                    AuthenticationSession.get().setUserId(subject.getUserId());
                    AuthenticationSession.get().setLoginName(login.getObject());

                    throw new RestartResponseException(new NewUserDevicePage());

                }

            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
