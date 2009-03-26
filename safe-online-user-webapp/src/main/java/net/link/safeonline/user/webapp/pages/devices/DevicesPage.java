/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.webapp.pages.devices;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.data.DeviceRegistrationDO;
import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.user.webapp.template.NavigationPanel.Panel;
import net.link.safeonline.user.webapp.util.DeviceEntry;
import net.link.safeonline.user.webapp.util.DeviceOperationUtils;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.components.attribute.AttributeOutputPanel;
import net.link.safeonline.wicket.tools.RedirectResponseException;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


@RequireLogin(loginPage = MainPage.class)
public class DevicesPage extends UserTemplatePage {

    static final Log           LOG                   = LogFactory.getLog(DevicesPage.class);

    private static final long  serialVersionUID      = 1L;

    public static final String PATH                  = "device_page";

    public static final String DEVICES_ID            = "devices";
    public static final String NAME_ID               = "name";
    public static final String REGISTER_LINK_ID      = "register";

    public static final String REGISTERED_DEVICES_ID = "registered_devices";
    public static final String REGISTERED_NAME_ID    = "registered_name";
    public static final String INFORMATION_ID        = "information";
    public static final String REMOVE_LINK_ID        = "remove";
    public static final String UPDATE_LINK_ID        = "update";
    public static final String DISABLE_LINK_ID       = "disable";
    public static final String ENABLE_LINK_ID        = "enable";

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    DevicePolicyService        devicePolicyService;

    @EJB(mappedName = DeviceService.JNDI_BINDING)
    DeviceService              deviceService;

    @EJB(mappedName = SubjectManager.JNDI_BINDING)
    SubjectManager             subjectManager;


    public DevicesPage() {

        super(Panel.devices);

        getSidebar(localize("helpDevices"), false);

        getContent().add(new ErrorFeedbackPanel("feedback"));

        final SubjectEntity subject = subjectManager.getCallerSubject();

        /*
         * Add devices
         */
        getContent().add(new ListView<DeviceEntry>(DEVICES_ID, getDevices()) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(ListItem<DeviceEntry> item) {

                final DeviceEntry deviceEntry = item.getModelObject();
                item.add(new Label(NAME_ID, deviceEntry.getFriendlyName()));

                item.add(new Link<String>(REGISTER_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        throw new RedirectResponseException(new IRequestTarget() {

                            public void detach(RequestCycle requestCycle) {

                            }

                            public void respond(RequestCycle requestCycle) {

                                LOG.debug("register device: " + deviceEntry.getFriendlyName());
                                register(subject, deviceEntry);
                            }
                        });
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return deviceEntry.getDevice().isRegistrable();
                    }

                });
            }
        });

        /*
         * Add registered devices
         */
        final List<DeviceRegistrationDO> deviceRegistrations = getDeviceRegistrations(subject);
        getContent().add(new ListView<DeviceRegistrationDO>(REGISTERED_DEVICES_ID, deviceRegistrations) {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(ListItem<DeviceRegistrationDO> item) {

                final DeviceRegistrationDO deviceRegistration = item.getModelObject();
                item.add(new Label(REGISTERED_NAME_ID, deviceRegistration.getFriendlyName()));
                item.add(new AttributeOutputPanel(INFORMATION_ID, deviceRegistration.getAttribute()));
                item.add(new Link<String>(REMOVE_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        throw new RedirectResponseException(new IRequestTarget() {

                            public void detach(RequestCycle requestCycle) {

                            }

                            public void respond(RequestCycle requestCycle) {

                                DevicesPage.this.deviceOperation(subject, deviceRegistration, DeviceOperationType.REMOVE);
                            }
                        });
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return deviceRegistration.getDevice().isRemovable() && deviceRemovalDisablingAllowed(deviceRegistrations);
                    }
                });

                item.add(new Link<String>(UPDATE_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        throw new RedirectResponseException(new IRequestTarget() {

                            public void detach(RequestCycle requestCycle) {

                            }

                            public void respond(RequestCycle requestCycle) {

                                DevicesPage.this.deviceOperation(subject, deviceRegistration, DeviceOperationType.UPDATE);
                            }
                        });
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return deviceRegistration.getDevice().isUpdatable();
                    }
                });

                item.add(new Link<String>(DISABLE_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        throw new RedirectResponseException(new IRequestTarget() {

                            public void detach(RequestCycle requestCycle) {

                            }

                            public void respond(RequestCycle requestCycle) {

                                DevicesPage.this.deviceOperation(subject, deviceRegistration, DeviceOperationType.DISABLE);
                            }
                        });
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return deviceRegistration.getDevice().isDisablable() && deviceRemovalDisablingAllowed(deviceRegistrations)
                                && !deviceRegistration.isDisabled();
                    }
                });

                item.add(new Link<String>(ENABLE_LINK_ID) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public void onClick() {

                        throw new RedirectResponseException(new IRequestTarget() {

                            public void detach(RequestCycle requestCycle) {

                            }

                            public void respond(RequestCycle requestCycle) {

                                DevicesPage.this.deviceOperation(subject, deviceRegistration, DeviceOperationType.ENABLE);
                            }
                        });
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return deviceRegistration.getDevice().isEnablable() && deviceRegistration.isDisabled();
                    }
                });

            }

        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("devices");
    }

    private List<DeviceEntry> getDevices() {

        List<DeviceEntry> devices = new LinkedList<DeviceEntry>();
        List<DeviceEntity> deviceList = devicePolicyService.getDevices();
        for (DeviceEntity device : deviceList) {
            String deviceDescription = devicePolicyService.getDeviceDescription(device.getName(), getLocale());
            devices.add(new DeviceEntry(device, deviceDescription));
        }
        return devices;
    }

    private List<DeviceRegistrationDO> getDeviceRegistrations(SubjectEntity subject) {

        try {
            return deviceService.getDeviceRegistrations(subject, getLocale());
        } catch (SubjectNotFoundException e) {
            error(localize("errorSubjectNotFound"));
            return null;
        } catch (DeviceNotFoundException e) {
            error(localize("errorDeviceNotFound"));
            return null;
        } catch (PermissionDeniedException e) {
            error(localize("errorPermissionDenied"));
            return null;
        } catch (AttributeTypeNotFoundException e) {
            error(localize("errorAttributeTypeNotFound"));
            return null;
        }

    }

    void register(SubjectEntity subject, DeviceEntry deviceEntry) {

        try {
            String registrationURL = devicePolicyService.getRegistrationURL(deviceEntry.getDevice().getName());
            DeviceOperationUtils.redirect(WicketUtil.toServletRequest(), WicketUtil.toServletResponse(), registrationURL,
                    DeviceOperationType.REGISTER, deviceEntry.getDevice().getName(),
                    LoginManager.getAuthenticatedDevice(WicketUtil.toServletRequest()), subject.getUserId(), null, null);
        } catch (DeviceNotFoundException e) {
            error(localize("errorDeviceNotFound"));
            return;
        } catch (ServletException e) {
            error(localize("errorMessage"));
            return;
        }
    }

    boolean deviceRemovalDisablingAllowed(List<DeviceRegistrationDO> deviceRegistrations) {

        if (deviceRegistrations.size() == 1)
            return false;

        for (DeviceRegistrationDO deviceRegistration : deviceRegistrations) {
            if (!deviceRegistration.isDisabled())
                return true;
        }
        return false;
    }

    void deviceOperation(SubjectEntity subject, DeviceRegistrationDO deviceRegistration, DeviceOperationType operation) {

        LOG.debug("perform " + operation.toString() + " on device " + deviceRegistration.getFriendlyName());
        String url = null;
        switch (operation) {
            case DISABLE:
                url = deviceRegistration.getDevice().getDisableURL();
            break;
            case ENABLE:
                url = deviceRegistration.getDevice().getEnableURL();
            break;
            case NEW_ACCOUNT_REGISTER:
                return;
            case REGISTER:
                return;
            case REMOVE:
                url = deviceRegistration.getDevice().getRemovalURL();
            break;
            case UPDATE:
                url = deviceRegistration.getDevice().getUpdateURL();
            break;
        }

        try {
            DeviceOperationUtils.redirect(WicketUtil.toServletRequest(), WicketUtil.toServletResponse(), url, operation,
                    deviceRegistration.getDevice().getName(), LoginManager.getAuthenticatedDevice(WicketUtil.toServletRequest()),
                    subject.getUserId(), deviceRegistration.getId(), deviceRegistration.getAttribute());
        } catch (ServletException e) {
            error(localize("errorMessage"));
            return;
        }

    }
}
