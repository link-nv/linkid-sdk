/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.data.DeviceRegistrationDO;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.user.DeviceEntry;
import net.link.safeonline.user.DeviceOperationUtils;
import net.link.safeonline.user.Devices;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("devicesBean")
@LocalBinding(jndiBinding = Devices.JNDI_BINDING)
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DevicesBean implements Devices {

    private static final Log     LOG                            = LogFactory.getLog(DevicesBean.class);

    private static final String  DEVICES_LIST_NAME              = "devices";

    private static final String  DEVICE_REGISTRATIONS_LIST_NAME = "deviceRegistrations";

    private boolean              credentialCacheFlushRequired;

    @DataModel(DEVICES_LIST_NAME)
    List<DeviceEntry>            devices;

    @DataModelSelection(DEVICES_LIST_NAME)
    private DeviceEntry          selectedDevice;

    @DataModel(DEVICE_REGISTRATIONS_LIST_NAME)
    List<DeviceRegistrationDO>   deviceRegistrations;

    @DataModelSelection(DEVICE_REGISTRATIONS_LIST_NAME)
    private DeviceRegistrationDO selectedDeviceRegistration;


    @PostConstruct
    public void postConstructCallback() {

        credentialCacheFlushRequired = false;
    }


    @EJB(mappedName = SubjectManager.JNDI_BINDING)
    private SubjectManager      subjectManager;

    @EJB(mappedName = DeviceService.JNDI_BINDING)
    private DeviceService       deviceService;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService devicePolicyService;

    @In
    Context                     sessionContext;

    @In(create = true)
    FacesMessages               facesMessages;

    @In(value = LoginManager.AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE)
    String                      authenticatedDevice;


    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy callback");
        if (credentialCacheFlushRequired) {
            /*
             * We will set a HTTP session attribute to communicate to the JAAS Login Filter that the credential cache for the caller
             * principal needs to be flushed.
             */
            try {
                /*
                 * The JACC spec is not really clear here whether we can retrieve the HttpServletRequest also from within the EJB container,
                 * or only from within the Servlet container.
                 */
                HttpServletRequest httpServletRequest = (HttpServletRequest) PolicyContext.getContext(HttpServletRequest.class.getName());
                if (null != httpServletRequest) {
                    HttpSession session = httpServletRequest.getSession();
                    String attributeName = "FlushJBossCredentialCache";
                    session.setAttribute(attributeName, UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN);
                    LOG.debug("setting " + attributeName);
                } else {
                    LOG.debug("JACC HttpServletRequest is null");
                }
            } catch (PolicyContextException e) {
                LOG.error("JACC policy context error: " + e.getMessage());
                throw new EJBException("JACC policy context error: " + e.getMessage());
            }
        }
        credentialCacheFlushRequired = false;
    }

    private Locale getViewLocale() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        return viewLocale;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    @Factory(DEVICE_REGISTRATIONS_LIST_NAME)
    public List<DeviceRegistrationDO> deviceRegistrationsFactory()
            throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException {

        Locale locale = getViewLocale();
        LOG.debug("device registrations factory");
        SubjectEntity subject = subjectManager.getCallerSubject();
        deviceRegistrations = deviceService.getDeviceRegistrations(subject, locale);
        return deviceRegistrations;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    @Factory(DEVICES_LIST_NAME)
    public List<DeviceEntry> devicesFactory()
            throws SubjectNotFoundException, DeviceNotFoundException {

        Locale locale = getViewLocale();
        devices = new LinkedList<DeviceEntry>();
        List<DeviceEntity> deviceList = devicePolicyService.getDevices();
        for (DeviceEntity device : deviceList) {
            String deviceDescription = devicePolicyService.getDeviceDescription(device.getName(), locale);
            devices.add(new DeviceEntry(device, deviceDescription));
        }
        return devices;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String register()
            throws DeviceNotFoundException, IOException {

        LOG.debug("register device: " + selectedDevice.getFriendlyName());
        String userId = subjectManager.getCallerSubject().getUserId();

        String registrationURL = devicePolicyService.getRegistrationURL(selectedDevice.getDevice().getName());
        DeviceOperationUtils.redirect(registrationURL, DeviceOperationType.REGISTER, selectedDevice.getDevice().getName(),
                authenticatedDevice, userId, null, null);
        return null;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String removeDevice()
            throws DeviceNotFoundException, IOException {

        if (!deviceRemovalDisablingAllowed()) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
            return null;
        }
        LOG.debug("remove device: " + selectedDeviceRegistration.getFriendlyName());
        String userId = subjectManager.getCallerSubject().getUserId();
        String removalURL = selectedDeviceRegistration.getDevice().getRemovalURL();

        DeviceOperationUtils.redirect(removalURL, DeviceOperationType.REMOVE, selectedDeviceRegistration.getDevice().getName(),
                authenticatedDevice, userId, selectedDeviceRegistration.getId(), selectedDeviceRegistration.getAttribute());
        return null;
    }

    private boolean deviceRemovalDisablingAllowed() {

        if (deviceRegistrations.size() == 1)
            return false;

        for (DeviceRegistrationDO deviceRegistration : deviceRegistrations) {
            if (!deviceRegistration.isDisabled())
                return true;
        }

        return false;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String updateDevice()
            throws DeviceNotFoundException, IOException {

        LOG.debug("update device: " + selectedDeviceRegistration.getFriendlyName());
        String userId = subjectManager.getCallerSubject().getUserId();
        String updateURL = selectedDeviceRegistration.getDevice().getUpdateURL();

        DeviceOperationUtils.redirect(updateURL, DeviceOperationType.UPDATE, selectedDeviceRegistration.getDevice().getName(),
                authenticatedDevice, userId, selectedDeviceRegistration.getId(), selectedDeviceRegistration.getAttribute());
        return null;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String disableDevice()
            throws DeviceNotFoundException, IOException, SubjectNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException {

        if (!deviceRemovalDisablingAllowed()) {
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
            return null;
        }

        LOG.debug("disable device: " + selectedDeviceRegistration.getFriendlyName());
        String userId = subjectManager.getCallerSubject().getUserId();

        DeviceOperationUtils.redirect(selectedDeviceRegistration.getDevice().getDisableURL(), DeviceOperationType.DISABLE,
                selectedDeviceRegistration.getDevice().getName(), authenticatedDevice, userId, selectedDeviceRegistration.getId(),
                selectedDeviceRegistration.getAttribute());
        return null;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String enableDevice()
            throws DeviceNotFoundException, IOException, SubjectNotFoundException, AttributeTypeNotFoundException {

        LOG.debug("enable device: " + selectedDeviceRegistration.getFriendlyName());
        String userId = subjectManager.getCallerSubject().getUserId();

        DeviceOperationUtils.redirect(selectedDeviceRegistration.getDevice().getEnableURL(), DeviceOperationType.ENABLE,
                selectedDeviceRegistration.getDevice().getName(), authenticatedDevice, userId, selectedDeviceRegistration.getId(),
                selectedDeviceRegistration.getAttribute());
        return null;
    }
}
