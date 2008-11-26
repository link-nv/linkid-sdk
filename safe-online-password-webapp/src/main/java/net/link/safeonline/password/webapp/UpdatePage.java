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
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.common.HelpPage;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;


public class UpdatePage extends TemplatePage {

    private static final long       serialVersionUID     = 1L;

    static final Log                LOG                  = LogFactory.getLog(UpdatePage.class);

    public static final String      UPDATE_FORM_ID       = "update_form";

    public static final String      OLDPASSWORD_FIELD_ID = "oldpassword";

    public static final String      PASSWORD1_FIELD_ID   = "password1";

    public static final String      PASSWORD2_FIELD_ID   = "password2";

    public static final String      SAVE_BUTTON_ID       = "save";

    public static final String      CANCEL_BUTTON_ID     = "cancel";

    @EJB(mappedName = PasswordDeviceService.JNDI_BINDING)
    transient PasswordDeviceService passwordDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService  samlAuthorityService;

    ProtocolContext                 protocolContext;


    public UpdatePage() {

        super();

        this.protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        addHeader(this, false);

        getSidebar().add(new Link<String>("help") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                setResponsePage(new HelpPage(getPage()));

            }

        });

        getContent().add(new RegistrationForm(UPDATE_FORM_ID));

    }


    class RegistrationForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        String                    oldpassword;

        String                    password1;

        String                    password2;


        @SuppressWarnings("unchecked")
        public RegistrationForm(String id) {

            super(id);

            final PasswordTextField oldpasswordField = new PasswordTextField(OLDPASSWORD_FIELD_ID, new PropertyModel<String>(this,
                    "oldpassword"));
            add(oldpasswordField);
            add(new ErrorComponentFeedbackLabel("oldpassword_feedback", oldpasswordField));

            final PasswordTextField password1Field = new PasswordTextField(PASSWORD1_FIELD_ID, new PropertyModel<String>(this, "password1"));
            add(password1Field);
            add(new ErrorComponentFeedbackLabel("password1_feedback", password1Field));

            final PasswordTextField password2Field = new PasswordTextField(PASSWORD2_FIELD_ID, new PropertyModel<String>(this, "password2"));
            add(password2Field);
            add(new ErrorComponentFeedbackLabel("password2_feedback", password2Field));

            add(new EqualPasswordInputValidator(password1Field, password2Field));

            add(new Button(SAVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("update password for " + UpdatePage.this.protocolContext.getSubject());

                    try {
                        UpdatePage.this.passwordDeviceService.update(UpdatePage.this.protocolContext.getSubject(),
                                RegistrationForm.this.oldpassword, RegistrationForm.this.password1);
                    } catch (SubjectNotFoundException e) {
                        password1Field.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "update: subject not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        password1Field.error(getLocalizer().getString("errorOldPasswordNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "update: device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (PermissionDeniedException e) {
                        oldpasswordField.error(getLocalizer().getString("errorOldPasswordNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "register: device not found",
                                LogLevelType.ERROR);
                        return;
                    }

                    UpdatePage.this.protocolContext.setSuccess(true);
                    exit();
                }

            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    UpdatePage.this.protocolContext.setSuccess(false);
                    exit();
                }

            };
            cancel.setDefaultFormProcessing(false);
            add(cancel);

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }


    public void exit() {

        this.protocolContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
