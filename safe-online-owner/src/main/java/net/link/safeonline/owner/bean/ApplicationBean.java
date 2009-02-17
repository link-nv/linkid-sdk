/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.owner.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.owner.Application;
import net.link.safeonline.owner.DeviceEntry;
import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.service.DeviceService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;


@Stateful
@Name("ownerApplication")
@LocalBinding(jndiBinding = Application.JNDI_BINDING)
@SecurityDomain(OwnerConstants.SAFE_ONLINE_OWNER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class ApplicationBean implements Application {

    private static final Log      LOG                                     = LogFactory.getLog(ApplicationBean.class);

    private static final String   selectedApplicationUsageAgreementsModel = "selectedApplicationUsageAgreements";

    @EJB(mappedName = ApplicationService.JNDI_BINDING)
    private ApplicationService    applicationService;

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    private UsageAgreementService usageAgreementService;

    @EJB(mappedName = SubscriptionService.JNDI_BINDING)
    private SubscriptionService   subscriptionService;

    @EJB(mappedName = DeviceService.JNDI_BINDING)
    private DeviceService         deviceService;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService   devicePolicyService;

    @In(create = true)
    FacesMessages                 facesMessages;

    @SuppressWarnings("unused")
    @Out
    private long                  numberOfSubscriptions;

    private String                name;

    private String                friendlyName;


    /*
     * Lifecycle
     */
    @Remove
    @Destroy
    public void destroyCallback() {

        name = null;
        friendlyName = null;

    }


    /*
     * Seam Data models
     */
    @SuppressWarnings("unused")
    @DataModel
    private List<ApplicationEntity>                 ownerApplicationList;

    @DataModelSelection("ownerApplicationList")
    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationEntity                       selectedApplication;

    @SuppressWarnings("unused")
    @DataModel(value = "selectedApplicationIdentity")
    private Set<ApplicationIdentityAttributeEntity> selectedApplicationIdentity;

    @SuppressWarnings("unused")
    @DataModel(value = selectedApplicationUsageAgreementsModel)
    private List<UsageAgreementEntity>              selectedApplicationUsageAgreements;

    @DataModelSelection(selectedApplicationUsageAgreementsModel)
    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private UsageAgreementEntity                    selectedUsageAgreement;

    @SuppressWarnings("unused")
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity                    draftUsageAgreement;

    @SuppressWarnings("unused")
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity                    currentUsageAgreement;

    @DataModel
    private List<DeviceEntry>                       allowedDevices;


    /*
     * Seam Data model Factories
     */
    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    @Factory("ownerApplicationList")
    public void applicationListFactory()
            throws ApplicationOwnerNotFoundException {

        LOG.debug("application list factory");
        ownerApplicationList = applicationService.getOwnedApplications();
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    @Factory("allowedDevices")
    public void allowedDevices() {

        if (selectedApplication == null)
            return;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();

        List<DeviceEntity> deviceList = deviceService.listDevices();
        List<AllowedDeviceEntity> allowedDeviceList = deviceService.listAllowedDevices(selectedApplication);

        allowedDevices = new ArrayList<DeviceEntry>();

        boolean defaultValue = false;

        for (DeviceEntity deviceEntity : deviceList) {
            String deviceDescription = devicePolicyService.getDeviceDescription(deviceEntity.getName(), viewLocale);
            allowedDevices.add(new DeviceEntry(deviceEntity, deviceDescription, defaultValue, 0));
        }

        for (AllowedDeviceEntity allowedDevice : allowedDeviceList) {
            for (DeviceEntry deviceEntry : allowedDevices) {
                if (deviceEntry.getDevice().equals(allowedDevice.getDevice())) {
                    deviceEntry.setAllowed(true);
                    deviceEntry.setWeight(allowedDevice.getWeight());
                }
            }
        }
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    @Factory(selectedApplicationUsageAgreementsModel)
    public void usageAgreementListFactory()
            throws ApplicationNotFoundException, PermissionDeniedException {

        if (null == selectedApplication)
            return;
        LOG.debug("usage agreement list factory");
        selectedApplicationUsageAgreements = usageAgreementService.getUsageAgreements(selectedApplication.getId());
    }

    /*
     * Accessors
     */
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
    }

    /*
     * Actions
     */
    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String view()
            throws ApplicationNotFoundException, PermissionDeniedException, ApplicationIdentityNotFoundException {

        long applicationId = selectedApplication.getId();
        LOG.debug("view: " + applicationId);
        numberOfSubscriptions = subscriptionService.getNumberOfSubscriptions(applicationId);
        selectedApplicationIdentity = applicationService.getCurrentApplicationIdentity(applicationId);
        return "view-application";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String edit() {

        LOG.debug("edit: " + selectedApplication.getName());
        name = selectedApplication.getName();
        friendlyName = selectedApplication.getFriendlyName();
        return "edit-application";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String save()
            throws ApplicationNotFoundException, PermissionDeniedException {

        long applicationId = selectedApplication.getId();
        String applicationDescription = selectedApplication.getDescription();
        boolean deviceRestriction = selectedApplication.isDeviceRestriction();
        LOG.debug("save: " + applicationId);
        LOG.debug("description: " + applicationDescription);

        if (!selectedApplication.getName().equals(name)) {
            try {
                applicationService.updateApplicationName(applicationId, name);
            } catch (ExistingApplicationException e) {
                LOG.debug("application already exists: " + name);
                facesMessages.addToControlFromResourceBundle("name", FacesMessage.SEVERITY_ERROR, "errorApplicationAlreadyExists", name);
                return null;
            }
        }
        if (null != friendlyName) {
            applicationService.updateApplicationFriendlyName(applicationId, friendlyName);
        }

        List<AllowedDeviceEntity> allowedDeviceList = new ArrayList<AllowedDeviceEntity>();
        for (DeviceEntry deviceEntry : allowedDevices) {
            if (deviceEntry.isAllowed() == true) {
                AllowedDeviceEntity device = new AllowedDeviceEntity(selectedApplication, deviceEntry.getDevice(), deviceEntry.getWeight());
                allowedDeviceList.add(device);
            }
        }

        applicationService.setApplicationDescription(applicationId, applicationDescription);
        applicationService.setApplicationDeviceRestriction(applicationId, deviceRestriction);
        deviceService.setAllowedDevices(selectedApplication, allowedDeviceList);
        return "saved";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String viewStats() {

        return "viewstats";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String viewUsageAgreement() {

        LOG.debug("view usage agreement for application: " + selectedApplication.getName() + ", version="
                + selectedUsageAgreement.getUsageAgreementVersion());
        return "view-usage-agreement";
    }

    @RolesAllowed(OwnerConstants.OWNER_ROLE)
    public String editUsageAgreement()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("edit usage agreement for application: " + selectedApplication.getName());
        draftUsageAgreement = usageAgreementService.getDraftUsageAgreement(selectedApplication.getId());
        currentUsageAgreement = usageAgreementService.getCurrentUsageAgreement(selectedApplication.getId());
        return "edit-usage-agreement";
    }
}
