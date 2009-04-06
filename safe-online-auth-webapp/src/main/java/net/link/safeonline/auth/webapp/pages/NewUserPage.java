/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import javax.ejb.EJB;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.CustomRequiredTextField;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressRegistrationPanel;
import net.link.safeonline.webapp.template.SideLink;
import net.link.safeonline.webapp.template.SidebarBorder;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;


public class NewUserPage extends AuthenticationTemplatePage {

    static final Log           LOG                 = LogFactory.getLog(NewUserDevicePage.class);

    private static final long  serialVersionUID    = 1L;

    public static final String NEW_USER_FORM_ID    = "new_user_form";
    public static final String LOGIN_NAME_FIELD_ID = "loginName";
    public static final String CAPTCHA_FIELD_ID    = "captcha";
    public static final String CAPTCHA_IMAGE_ID    = "captchaImage";
    public static final String CAPTCHA_REFRESH_ID  = "captchaRefresh";
    public static final String REGISTER_BUTTON_ID  = "register";

    @EJB(mappedName = UserRegistrationService.JNDI_BINDING)
    UserRegistrationService    userRegistrationService;


    public NewUserPage() {

        Link<String> existingUserLink = new Link<String>(SidebarBorder.LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RestartResponseException(new MainPage());
            }
        };
        getSidebar(localize("helpNewUser"), new SideLink(existingUserLink, localize("existingUser")));
        getHeader();

        getContent().add(new ProgressRegistrationPanel("progress", ProgressRegistrationPanel.stage.choose));

        getContent().add(new NewUserForm(NEW_USER_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        return localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
    }


    class NewUserForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             login;

        Model<String>             captcha;


        @SuppressWarnings("unchecked")
        public NewUserForm(String id) {

            super(id);
            setMarkupId(id);

            final CustomRequiredTextField<String> loginField = new CustomRequiredTextField<String>(LOGIN_NAME_FIELD_ID,
                    login = new Model<String>());
            loginField.setRequired(true);
            loginField.setRequiredMessageKey("errorMissingLoginName");
            add(loginField);
            focus(loginField);
            add(new ErrorComponentFeedbackLabel("login_feedback", loginField));

            final CustomRequiredTextField<String> captchaField = new CustomRequiredTextField<String>(CAPTCHA_FIELD_ID,
                    captcha = new Model<String>());
            captchaField.setRequired(true);
            captchaField.setRequiredMessageKey("errorMissingCaptcha");
            add(captchaField);
            add(new ErrorComponentFeedbackLabel("captcha_feedback", captchaField));

            final Image captchaImage = new Image(CAPTCHA_IMAGE_ID, "override");
            captchaImage.add(new SimpleAttributeModifier("src", WicketUtil.getServletRequest().getContextPath() + "/captcha.jpg?cacheid="
                    + Math.random() * 1000000));
            captchaImage.setOutputMarkupId(true);
            add(captchaImage);

            add(new AjaxLink(CAPTCHA_REFRESH_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick(AjaxRequestTarget target) {

                    captchaImage.add(new SimpleAttributeModifier("src", WicketUtil.getServletRequest().getContextPath()
                            + "/captcha.jpg?cacheid=" + Math.random() * 1000000));
                    target.addComponent(captchaImage);
                }
            });

            add(new Button(REGISTER_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("register: " + login);

                    HelpdeskLogger.add("account creation: login=" + login, LogLevelType.INFO);

                    String validCaptcha = (String) WicketUtil.getHttpSession().getAttribute(
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

                    LoginManager.setUserId(WicketUtil.getHttpSession(), subject.getUserId());
                    LoginManager.setLogin(WicketUtil.getHttpSession(), login.getObject());

                    throw new RestartResponseException(new NewUserDevicePage());

                }

            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
