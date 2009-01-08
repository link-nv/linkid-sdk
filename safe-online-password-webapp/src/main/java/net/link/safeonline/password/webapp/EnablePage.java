/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.password.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.Model;


public class EnablePage extends TemplatePage {

    private static final long       serialVersionUID  = 1L;

    static final Log                LOG               = LogFactory.getLog(EnablePage.class);

    public static final String      ENABLE_FORM_ID    = "enable_form";
    public static final String      PASSWORD_FIELD_ID = "password";
    public static final String      ENABLE_BUTTON_ID  = "enable";
    public static final String      CANCEL_BUTTON_ID  = "cancel";

    @EJB(mappedName = PasswordDeviceService.JNDI_BINDING)
    transient PasswordDeviceService passwordDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService  samlAuthorityService;

    ProtocolContext                 protocolContext;


    public EnablePage() {

        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        getHeader();
        getSidebar();
        getContent().add(new EnableForm(ENABLE_FORM_ID));
    }


    class EnableForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             password;


        @SuppressWarnings("unchecked")
        public EnableForm(String id) {

            super(id);

            final PasswordTextField passwordField = new PasswordTextField(PASSWORD_FIELD_ID, password = new Model<String>());
            add(passwordField);
            add(new ErrorComponentFeedbackLabel("password_feedback", passwordField));

            add(new Button(ENABLE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("enable password for " + protocolContext.getSubject());

                    try {
                        passwordDeviceService.enable(protocolContext.getSubject(), password.getObject());
                    } catch (SubjectNotFoundException e) {
                        passwordField.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: subject not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        passwordField.error(getLocalizer().getString("errorPasswordNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (PermissionDeniedException e) {
                        passwordField.error(getLocalizer().getString("errorPasswordNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: permission denied: "
                                + e.getMessage(), LogLevelType.ERROR);
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
    }


    public void exit() {

        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
