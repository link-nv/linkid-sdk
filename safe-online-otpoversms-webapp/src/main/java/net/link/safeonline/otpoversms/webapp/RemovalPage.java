/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.common.HelpPage;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;


public class RemovalPage extends TemplatePage {

    private static final long         serialVersionUID = 1L;

    static final Log                  LOG              = LogFactory.getLog(RemovalPage.class);

    public static final String        REMOVAL_FORM_ID  = "removal_form";

    public static final String        PIN_FIELD_ID     = "pin";

    public static final String        REMOVE_BUTTON_ID = "remove";

    public static final String        CANCEL_BUTTON_ID = "cancel";

    @EJB
    transient OtpOverSmsDeviceService otpOverSmsDeviceService;

    @EJB
    transient SamlAuthorityService    samlAuthorityService;

    ProtocolContext                   protocolContext;


    public RemovalPage() {

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

        String title = getLocalizer().getString("removeaction", this) + " " + getLocalizer().getString("mobile", this) + " "
                + this.protocolContext.getAttribute();
        getContent().add(new Label("title", title));

        getContent().add(new RemovalForm(REMOVAL_FORM_ID));

    }


    class RemovalForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        String                    pin;


        @SuppressWarnings("unchecked")
        public RemovalForm(String id) {

            super(id);

            final PasswordTextField passwordField = new PasswordTextField(PIN_FIELD_ID, new PropertyModel<String>(this, "pin"));
            add(passwordField);
            add(new ErrorComponentFeedbackLabel("pin_feedback", passwordField));

            add(new Button(REMOVE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("remove mobile " + RemovalPage.this.protocolContext.getAttribute() + " for "
                            + RemovalPage.this.protocolContext.getSubject());

                    try {
                        RemovalPage.this.otpOverSmsDeviceService.remove(RemovalPage.this.protocolContext.getSubject(),
                                RemovalPage.this.protocolContext.getAttribute(), RemovalForm.this.pin);
                    } catch (SubjectNotFoundException e) {
                        passwordField.error(getLocalizer().getString("errorSubjectNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "remove: subject not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        passwordField.error(getLocalizer().getString("errorDeviceNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "remove: device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (PermissionDeniedException e) {
                        passwordField.error(getLocalizer().getString("errorPinNotCorrect", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "remove: device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (AttributeTypeNotFoundException e) {
                        passwordField.error(getLocalizer().getString("errorAttributeTypeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "remove: attribute type not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (AttributeNotFoundException e) {
                        passwordField.error(getLocalizer().getString("errorAttributeNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "remove: attribute not found",
                                LogLevelType.ERROR);
                        return;
                    }

                    RemovalPage.this.protocolContext.setSuccess(true);
                    exit();
                }

            });

            Button cancel = new Button(CANCEL_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    RemovalPage.this.protocolContext.setSuccess(false);
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
