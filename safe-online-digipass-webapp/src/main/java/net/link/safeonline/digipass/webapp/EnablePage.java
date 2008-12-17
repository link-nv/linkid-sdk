/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass.webapp;

import javax.ejb.EJB;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.common.HelpPage;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
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


public class EnablePage extends TemplatePage {

    private static final long       serialVersionUID = 1L;

    static final Log                LOG              = LogFactory.getLog(EnablePage.class);

    public static final String      ENABLE_FORM_ID   = "enable_form";
    public static final String      TOKEN_FIELD_ID   = "token";
    public static final String      ENABLE_BUTTON_ID = "enable";
    public static final String      CANCEL_BUTTON_ID = "cancel";

    @EJB(mappedName = DigipassDeviceService.JNDI_BINDING)
    transient DigipassDeviceService digipassDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService  samlAuthorityService;

    ProtocolContext                 protocolContext;


    public EnablePage() {

        this.protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        getHeader(false);
        getSidebar().add(new Link<String>("help") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                setResponsePage(new HelpPage(getPage()));

            }

        });

        String title = getLocalizer().getString("enable", this) + " " + getLocalizer().getString("digipass", this) + " "
                + this.protocolContext.getAttribute();
        getContent().add(new Label("title", title));
        getContent().add(new EnableForm(ENABLE_FORM_ID));
    }


    class EnableForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             token;


        @SuppressWarnings("unchecked")
        public EnableForm(String id) {

            super(id);

            final TextField<String> tokenField = new TextField<String>(TOKEN_FIELD_ID, this.token = new Model<String>());
            tokenField.setRequired(true);
            add(tokenField);

            add(new Button(ENABLE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("enable digipass " + EnablePage.this.protocolContext.getAttribute());

                    try {
                        String userId = EnablePage.this.digipassDeviceService.enable(EnablePage.this.protocolContext.getSubject(),
                                EnablePage.this.protocolContext.getAttribute(), EnableForm.this.token.getObject());
                        if (null == userId) {
                            EnableForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                            HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: authentication failed: "
                                    + EnablePage.this.protocolContext.getSubject(), LogLevelType.ERROR);
                            return;
                        }
                    } catch (SubjectNotFoundException e) {
                        EnableForm.this.error(getLocalizer().getString("digipassNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: subject not found for "
                                + EnablePage.this.protocolContext.getSubject(), LogLevelType.ERROR);
                        return;
                    } catch (DeviceNotFoundException e) {
                        EnableForm.this.error(getLocalizer().getString("digipassAuthenticationFailed", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: digipass device not found",
                                LogLevelType.ERROR);
                        return;
                    } catch (DeviceRegistrationNotFoundException e) {
                        EnableForm.this.error(getLocalizer().getString("errorDeviceRegistrationNotFound", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: device registration not found",
                                LogLevelType.ERROR);
                        return;
                    }

                    EnablePage.this.protocolContext.setSuccess(true);
                    exit();
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
    }


    public void exit() {

        this.protocolContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
