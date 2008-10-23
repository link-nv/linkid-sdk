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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
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
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "DevicesBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class DevicesBean implements Devices {

    private static final Log     LOG                            = LogFactory.getLog(DevicesBean.class);

    private static final String  DEVICES_LIST_NAME              = "devices";

    private static final String  DEVICE_REGISTRATIONS_LIST_NAME = "deviceRegistrations";

    private String               oldPassword;

    private String               newPassword;

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

        this.credentialCacheFlushRequired = false;
    }


    @EJB
    private SubjectManager      subjectManager;

    @EJB
    private DeviceService       deviceService;

    @EJB
    private CredentialService   credentialService;

    @EJB
    private DevicePolicyService devicePolicyService;

    @In
    Context                     sessionContext;

    @In(create = true)
    FacesMessages               facesMessages;

    @In(value = LoginManager.AUTHENTICATED_DEVICE_SESSION_ATTRIBUTE)
    String                      authenticatedDevice;


    public String getNewPassword() {

        return this.newPassword;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public void setNewPassword(String newPassword) {

        this.newPassword = newPassword;
    }

    public String getOldPassword() {

        return this.oldPassword;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public void setOldPassword(String oldPassword) {

        this.oldPassword = oldPassword;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    @ErrorHandling( {
            @Error(exceptionClass = PermissionDeniedException.class, messageId = "errorOldPasswordNotCorrect", fieldId = "newpassword"),
            @Error(exceptionClass = DeviceNotFoundException.class, messageId = "errorOldPasswordNotFound", fieldId = "newpassword") })
    public String registerPassword() throws SubjectNotFoundException, PermissionDeniedException,
            DeviceNotFoundException {

        this.credentialService.registerPassword(this.newPassword);
        this.credentialCacheFlushRequired = true;
        LOG.debug("returning success");
        return "success";

    }

    @RolesAllowed(UserConstants.USER_ROLE)
    @ErrorHandling( {
            @Error(exceptionClass = PermissionDeniedException.class, messageId = "errorOldPasswordNotCorrect", fieldId = "oldpassword"),
            @Error(exceptionClass = DeviceNotFoundException.class, messageId = "errorOldPasswordNotFound", fieldId = "oldpassword") })
    public String changePassword() throws SubjectNotFoundException, PermissionDeniedException, DeviceNotFoundException {

        this.credentialService.changePassword(this.oldPassword, this.newPassword);
        this.credentialCacheFlushRequired = true;
        LOG.debug("returning success");
        return "success";
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    @ErrorHandling( {
            @Error(exceptionClass = PermissionDeniedException.class, messageId = "errorOldPasswordNotCorrect", fieldId = "oldpassword"),
            @Error(exceptionClass = DeviceNotFoundException.class, messageId = "errorOldPasswordNotFound", fieldId = "oldpassword") })
    public String removePassword() throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException {

        this.credentialService.removePassword(this.oldPassword);
        this.credentialCacheFlushRequired = true;
        return "success";
    }

    @Remove
    @Destroy
    public void destroyCallback() {

        LOG.debug("destroy callback");
        if (this.credentialCacheFlushRequired) {
            /*
             * We will set a HTTP session attribute to communicate to the JAAS Login Filter that the credential cache
             * for the caller principal needs to be flushed.
             */
            try {
                /*
                 * The JACC spec is not really clear here whether we can retrieve the HttpServletRequest also from
                 * within the EJB container, or only from within the Servlet container.
                 */
                HttpServletRequest httpServletRequest = (HttpServletRequest) PolicyContext
                        .getContext(HttpServletRequest.class.getName());
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
        this.oldPassword = null;
        this.newPassword = null;
        this.credentialCacheFlushRequired = false;
    }

    private Locale getViewLocale() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();
        return viewLocale;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    @Factory(DEVICE_REGISTRATIONS_LIST_NAME)
    public List<DeviceRegistrationDO> deviceRegistrationsFactory() throws SubjectNotFoundException,
            DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException {

        Locale locale = getViewLocale();
        LOG.debug("device registrations factory");
        SubjectEntity subject = this.subjectManager.getCallerSubject();
        this.deviceRegistrations = this.deviceService.getDeviceRegistrations(subject, locale);
        return this.deviceRegistrations;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    @Factory(DEVICES_LIST_NAME)
    public List<DeviceEntry> devicesFactory() throws SubjectNotFoundException, DeviceNotFoundException {

        Locale locale = getViewLocale();
        this.devices = new LinkedList<DeviceEntry>();
        List<DeviceEntity> deviceList = this.devicePolicyService.getDevices();
        for (DeviceEntity device : deviceList) {
            String deviceDescription = this.devicePolicyService.getDeviceDescription(device.getName(), locale);
            if (device.getName().equals(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID) && isPasswordConfigured()) {
                this.devices.add(new DeviceEntry(device, deviceDescription, false));
            } else {
                this.devices.add(new DeviceEntry(device, deviceDescription));
            }
        }
        return this.devices;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String register() throws DeviceNotFoundException, IOException {

        LOG.debug("register device: " + this.selectedDevice.getFriendlyName());
        String userId = this.subjectManager.getCallerSubject().getUserId();

        String registrationURL = this.devicePolicyService.getRegistrationURL(this.selectedDevice.getDevice().getName());
        if (this.selectedDevice.getDevice().getName().equals(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            externalContext.redirect(registrationURL);
            return null;
        }
        DeviceOperationUtils.redirect(registrationURL, DeviceOperationType.REGISTER, this.selectedDevice.getDevice()
                .getName(), this.authenticatedDevice, userId, null);
        return null;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String removeDevice() throws DeviceNotFoundException, IOException {

        if (!deviceRemovalDisablingAllowed()) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
            return null;
        }
        LOG.debug("remove device: " + this.selectedDeviceRegistration.getFriendlyName());
        String userId = this.subjectManager.getCallerSubject().getUserId();
        String removalURL = this.selectedDeviceRegistration.getDevice().getRemovalURL();

        if (this.selectedDeviceRegistration.getDevice().getName().equals(
                SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            externalContext.redirect(removalURL);
            return null;
        }
        DeviceOperationUtils.redirect(removalURL, DeviceOperationType.REMOVE, this.selectedDeviceRegistration
                .getDevice().getName(), this.authenticatedDevice, userId, this.selectedDeviceRegistration
                .getAttribute());
        return null;
    }

    private boolean deviceRemovalDisablingAllowed() {

        if (this.deviceRegistrations.size() == 1)
            return false;
        return true;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String updateDevice() throws DeviceNotFoundException, IOException {

        LOG.debug("update device: " + this.selectedDeviceRegistration.getFriendlyName());
        String userId = this.subjectManager.getCallerSubject().getUserId();
        String updateURL = this.selectedDeviceRegistration.getDevice().getUpdateURL();

        if (this.selectedDeviceRegistration.getDevice().getName().equals(
                SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            externalContext.redirect(updateURL);
            return null;
        }
        DeviceOperationUtils.redirect(updateURL, DeviceOperationType.UPDATE, this.selectedDeviceRegistration
                .getDevice().getName(), this.authenticatedDevice, userId, this.selectedDeviceRegistration
                .getAttribute());
        return null;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public String disableDevice() throws DeviceNotFoundException, IOException, SubjectNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException {

        if (!deviceRemovalDisablingAllowed()) {
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
            return null;
        }

        LOG.debug("disable device: " + this.selectedDeviceRegistration.getFriendlyName());
        String userId = this.subjectManager.getCallerSubject().getUserId();

        if (this.selectedDeviceRegistration.getDevice().getName().equals(
                SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID)) {
            this.credentialService.disablePassword();
            deviceRegistrationsFactory();
            return "success";
        }

        DeviceOperationUtils.redirect(this.selectedDeviceRegistration.getDevice().getDisableURL(),
                DeviceOperationType.DISABLE, this.selectedDeviceRegistration.getDevice().getName(),
                this.authenticatedDevice, userId, this.selectedDeviceRegistration.getAttribute());
        return null;
    }

    @RolesAllowed(UserConstants.USER_ROLE)
    public boolean isPasswordConfigured() throws SubjectNotFoundException, DeviceNotFoundException {

        return this.credentialService.isPasswordConfigured();
    }
}
