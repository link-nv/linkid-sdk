/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp.pages;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.webapp.DeviceDO;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.wicket.tools.RedirectResponseException;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;


public class RegisterDevicePage extends AuthenticationTemplatePage {

    static final Log           LOG                        = LogFactory.getLog(RegisterDevicePage.class);

    private static final long  serialVersionUID           = 1L;

    public static final String PATH                       = "register-device";

    public static final String NEW_USER_LINK_ID           = "new_user";
    public static final String TRY_ANOTHER_DEVICE_LINK_ID = "try_another_device";

    public static final String REGISTER_DEVICE_FORM_ID    = "register_device_form";
    public static final String DEVICE_GROUP_ID            = "deviceGroup";
    public static final String DEVICES_ID                 = "devices";
    public static final String BACK_BUTTON_ID             = "back";
    public static final String NEXT_BUTTON_ID             = "next";

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    DevicePolicyService        devicePolicyService;

    List<DeviceDO>             devices;


    public RegisterDevicePage() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        devices = new LinkedList<DeviceDO>();
        List<DeviceEntity> deviceEntities;
        try {
            deviceEntities = devicePolicyService.getDevicePolicy(protocolContext.getApplicationId(), protocolContext.getRequiredDevices());
        } catch (ApplicationNotFoundException e) {
            error(localize("errorApplicationNotFound"));
            return;
        } catch (EmptyDevicePolicyException e) {
            error(localize("errorEmptyDevicePolicy"));
            return;
        }
        for (DeviceEntity deviceEntity : deviceEntities) {
            String friendlyName = devicePolicyService.getDeviceDescription(deviceEntity.getName(), getLocale());
            devices.add(new DeviceDO(deviceEntity, friendlyName));
        }

        getSidebar(localize("helpRegisterDevice"));

        getHeader();

        getContent().add(new RegisterDeviceForm(REGISTER_DEVICE_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession());
        String title = localize("%l: %s", "authenticatingFor", protocolContext.getApplicationFriendlyName());
        return title;
    }


    class RegisterDeviceForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<DeviceDO>           device;


        @SuppressWarnings("unchecked")
        public RegisterDeviceForm(String id) {

            super(id);
            setMarkupId(id);

            final RadioGroup<DeviceDO> deviceGroup = new RadioGroup(DEVICE_GROUP_ID, device = new Model<DeviceDO>());
            deviceGroup.setRequired(true);
            add(deviceGroup);
            add(new ErrorComponentFeedbackLabel("device_feedback", deviceGroup, new Model<String>(localize("errorDeviceSelection"))));

            ListView<DeviceDO> deviceView = new ListView<DeviceDO>(DEVICES_ID, devices) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(final ListItem<DeviceDO> deviceItem) {

                    Radio deviceRadio = new Radio("radio", deviceItem.getModel());
                    deviceRadio.setLabel(new Model<String>(deviceItem.getModelObject().getFriendlyName()));
                    deviceItem.add(new SimpleFormComponentLabel("name", deviceRadio));
                    deviceItem.add(deviceRadio);
                }

            };
            deviceGroup.add(deviceView);

            add(new Button(BACK_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onSubmit() {

                    throw new RestartResponseException(new AllDevicesPage());
                }

            }.setDefaultFormProcessing(false));

            add(new Button(NEXT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    final String deviceName = device.getObject().getDevice().getName();
                    LOG.debug("deviceNext: " + deviceName);

                    HelpdeskLogger.add("register device: " + deviceName, LogLevelType.INFO);

                    final String registrationURL;
                    try {
                        registrationURL = devicePolicyService.getRegistrationURL(deviceName);
                    } catch (DeviceNotFoundException e) {
                        RegisterDeviceForm.this.error(localize("errorDeviceNotFound"));
                        return;
                    }

                    throw new RedirectResponseException(new IRequestTarget() {

                        public void detach(RequestCycle requestCycle) {

                        }

                        public void respond(RequestCycle requestCycle) {

                            AuthenticationUtils.redirect(WicketUtil.getServletRequest(), WicketUtil.getServletResponse(), getLocale(),
                                    registrationURL, deviceName, LoginManager.getUserId(WicketUtil.getHttpSession()));

                        }

                    });
                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
