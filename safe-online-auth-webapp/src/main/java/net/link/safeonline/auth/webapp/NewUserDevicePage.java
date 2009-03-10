/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.ProgressRegistrationPanel;
import net.link.safeonline.wicket.tools.RedirectResponseException;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;


public class NewUserDevicePage extends AuthenticationTemplatePage {

    private static final long     serialVersionUID        = 1L;

    public static final String    PATH                    = "new-user-device";

    public static final String    LOGIN_LABEL_ID          = "loginLabel";

    public static final String    NEW_USER_DEVICE_FORM_ID = "new_user_device_form";
    public static final String    DEVICE_GROUP_ID         = "deviceGroup";
    public static final String    DEVICES_ID              = "devices";
    public static final String    NEXT_BUTTON_ID          = "next";

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    transient DevicePolicyService devicePolicyService;

    List<DeviceDO>                devices;


    public NewUserDevicePage() {

        devices = new LinkedList<DeviceDO>();
        List<DeviceEntity> deviceEntities = devicePolicyService.getDevices();
        for (DeviceEntity deviceEntity : deviceEntities) {
            String friendlyName = devicePolicyService.getDeviceDescription(deviceEntity.getName(), getLocale());
            devices.add(new DeviceDO(deviceEntity, friendlyName));
        }

        getSidebar(localize("helpNewUserDevice"));

        getHeader();

        getContent().add(new ProgressRegistrationPanel("progress", ProgressRegistrationPanel.stage.initial));

        String loginLabel = localize("%l: %s", "login", LoginManager.getLogin(WicketUtil.getHttpSession(getRequest())));
        getContent().add(new Label(LOGIN_LABEL_ID, loginLabel));

        getContent().add(new NewUserDeviceForm(NEW_USER_DEVICE_FORM_ID));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("createAccount");
    }


    class NewUserDeviceForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<DeviceDO>           device;


        @SuppressWarnings("unchecked")
        public NewUserDeviceForm(String id) {

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
                    deviceRadio.setEnabled(deviceItem.getModelObject().getDevice().isRegistrable());
                    deviceRadio.setLabel(new Model<String>(deviceItem.getModelObject().getFriendlyName()));
                    deviceItem.add(new SimpleFormComponentLabel("name", deviceRadio));
                    deviceItem.add(deviceRadio);
                }

            };
            deviceGroup.add(deviceView);

            add(new Button(NEXT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    final String deviceName = device.getObject().getDevice().getName();
                    LOG.debug("deviceNext: " + deviceName);

                    HelpdeskLogger.add("account creation: register device: " + deviceName, LogLevelType.INFO);

                    final String registrationURL;
                    try {
                        registrationURL = devicePolicyService.getRegistrationURL(deviceName);
                    } catch (DeviceNotFoundException e) {
                        NewUserDeviceForm.this.error(localize("errorDeviceNotFound"));
                        return;
                    }

                    throw new RedirectResponseException(new IRequestTarget() {

                        public void detach(RequestCycle requestCycle) {

                        }

                        public void respond(RequestCycle requestCycle) {

                            AuthenticationUtils.redirect(WicketUtil.toServletRequest(getRequest()),
                                    WicketUtil.toServletResponse(getResponse()), getLocale(), registrationURL, deviceName,
                                    LoginManager.getUserId(WicketUtil.getHttpSession(getRequest())));

                        }

                    });
                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
