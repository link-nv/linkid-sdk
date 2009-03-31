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
import net.link.safeonline.auth.webapp.DeviceDO;
import net.link.safeonline.auth.webapp.template.AuthenticationTemplatePage;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.webapp.components.ErrorComponentFeedbackLabel;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.SideLink;
import net.link.safeonline.webapp.template.SidebarBorder;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;


public class AllDevicesPage extends AuthenticationTemplatePage {

    static final Log           LOG                 = LogFactory.getLog(AllDevicesPage.class);

    private static final long  serialVersionUID    = 1L;

    public static final String PATH                = "all-devices";

    public static final String NEW_USER_LINK_ID    = "new_user";

    public static final String ALL_DEVICES_FORM_ID = "all_devices_form";
    public static final String DEVICE_GROUP_ID     = "deviceGroup";
    public static final String DEVICES_ID          = "devices";
    public static final String NEXT_BUTTON_ID      = "next";

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    DevicePolicyService        devicePolicyService;

    List<DeviceDO>             devices;


    public AllDevicesPage() {

        devices = new LinkedList<DeviceDO>();
        List<DeviceEntity> deviceEntities = devicePolicyService.getDevices();
        for (DeviceEntity deviceEntity : deviceEntities) {
            String friendlyName = devicePolicyService.getDeviceDescription(deviceEntity.getName(), getLocale());
            devices.add(new DeviceDO(deviceEntity, friendlyName));
        }

        Link<String> newUserLink = new Link<String>(SidebarBorder.LINK_ID) {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                throw new RestartResponseException(new NewUserPage());
            }
        };
        getSidebar(localize("helpAllDevices"), new SideLink(newUserLink, localize("newUser")));

        getHeader();

        getContent().add(new MainForm(ALL_DEVICES_FORM_ID));

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


    class MainForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<DeviceDO>           device;


        @SuppressWarnings("unchecked")
        public MainForm(String id) {

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

            add(new Button(NEXT_BUTTON_ID) {

                private static final long serialVersionUID = 1L;


                @Override
                public void onSubmit() {

                    final String deviceName = device.getObject().getDevice().getName();
                    LOG.debug("next: " + deviceName);

                    HelpdeskLogger.add("selected authentication device: " + deviceName, LogLevelType.INFO);

                    final String authenticationPath;
                    try {
                        authenticationPath = devicePolicyService.getAuthenticationURL(deviceName);
                    } catch (DeviceNotFoundException e) {
                        MainForm.this.error(localize("errorDeviceNotFound"));
                        return;
                    }
                    LOG.debug("authenticationPath: " + authenticationPath);

                    String requestPath = WicketUtil.toServletRequest(getRequest()).getRequestURL().toString();
                    if (!requestPath.endsWith(PATH)) {
                        requestPath += PATH;
                    }
                    final String finalRequestPath = requestPath;

                    throw new RedirectResponseException(new IRequestTarget() {

                        public void detach(RequestCycle requestCycle) {

                        }

                        public void respond(RequestCycle requestCycle) {

                            AuthenticationUtils.redirectAuthentication(WicketUtil.toServletRequest(getRequest()),
                                    WicketUtil.toServletResponse(getResponse()), getLocale(), finalRequestPath, authenticationPath,
                                    deviceName);

                        }

                    });

                }
            });

            add(new ErrorFeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this)));
        }
    }

}
