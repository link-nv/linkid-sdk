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
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;


public class EnablePage extends TemplatePage {

    private static final long       serialVersionUID = 1L;

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

        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        getHeader();
        getSidebar(localize("helpDigipassEnable"));

        String title = localize("%l %s", "digipass", protocolContext.getAttribute());
        getContent().add(new Label("title", title));
        getContent().add(new EnableForm(ENABLE_FORM_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("digipassEnable");
    }


    class EnableForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             token;


        @SuppressWarnings("unchecked")
        public EnableForm(String id) {

            super(id);

            final TextField<String> tokenField = new TextField<String>(TOKEN_FIELD_ID, token = new Model<String>());
            tokenField.setRequired(true);
            add(tokenField);
            focus(tokenField);

            add(new Button(ENABLE_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    LOG.debug("enable digipass " + protocolContext.getAttribute());

                    try {
                        String userId = digipassDeviceService.enable(protocolContext.getSubject(), protocolContext.getAttribute(),
                                token.getObject());
                        if (null == userId) {
                            EnableForm.this.error(getLocalizer().getString("authenticationFailedMsg", this));
                            HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: authentication failed: "
                                    + protocolContext.getSubject(), LogLevelType.ERROR);
                            return;
                        }
                    } catch (SubjectNotFoundException e) {
                        EnableForm.this.error(getLocalizer().getString("digipassNotRegistered", this));
                        HelpdeskLogger.add(WicketUtil.toServletRequest(getRequest()).getSession(), "enable: subject not found for "
                                + protocolContext.getSubject(), LogLevelType.ERROR);
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

                    protocolContext.setSuccess(true);
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

        protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
        getResponse().redirect("deviceexit");
        setRedirect(false);
    }
}
