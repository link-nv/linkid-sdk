/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.app.bean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationOwnerNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingApplicationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.ctrl.Convertor;
import net.link.safeonline.ctrl.ConvertorUtil;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationIdentityAttributeEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.app.Application;
import net.link.safeonline.oper.app.DeviceEntry;
import net.link.safeonline.oper.app.IdentityAttribute;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.service.SubjectService;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
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
@Name("operApplication")
@LocalBinding(jndiBinding = Application.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class ApplicationBean implements Application {

    private static final Log           LOG                                         = LogFactory.getLog(ApplicationBean.class);

    private static final String        NEW_IDENTITY_ATTRIBUTES_NAME                = "newIdentityAttributes";

    private static final String        IDENTITY_ATTRIBUTES_NAME                    = "identityAttributes";

    private static final String        OPER_APPLICATION_LIST_NAME                  = "operApplicationList";

    private static final String        APPLICATION_IDENTITY_ATTRIBUTES_NAME        = "applicationIdentityAttributes";

    private static final String        OPER_APPLICATION_ALLOWED_DEVICES_NAME       = "operAllowedDevices";

    private static final String        SELECTED_APPLICATION_USAGE_AGREEMENTS_MODEL = "operSelectedApplicationUsageAgreements";

    @EJB(mappedName = ApplicationService.JNDI_BINDING)
    private ApplicationService         applicationService;

    @EJB(mappedName = SubscriptionService.JNDI_BINDING)
    private SubscriptionService        subscriptionService;

    @EJB(mappedName = AttributeTypeService.JNDI_BINDING)
    private AttributeTypeService       attributeTypeService;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService             subjectService;

    @EJB(mappedName = UsageAgreementService.JNDI_BINDING)
    private UsageAgreementService      usageAgreementService;

    @EJB(mappedName = DeviceService.JNDI_BINDING)
    private DeviceService              deviceService;

    @EJB(mappedName = DevicePolicyService.JNDI_BINDING)
    private DevicePolicyService        devicePolicyService;

    private String                     name;

    private String                     friendlyName;

    private String                     description;

    private String                     applicationUrl;

    private UploadedFile               applicationLogoFile;

    private byte[]                     applicationLogo;

    private String                     applicationOwner;

    private UploadedFile               upFile;

    private boolean                    idmapping;

    private String                     applicationIdScope;

    private boolean                    skipMessageIntegrityCheck;

    private boolean                    deviceRestriction;

    private boolean                    ssoEnabled;

    private String                     ssoLogoutUrl;

    @SuppressWarnings("unused")
    @Out
    private long                       numberOfSubscriptions;

    @In(create = true)
    FacesMessages                      facesMessages;

    @DataModel(NEW_IDENTITY_ATTRIBUTES_NAME)
    private List<IdentityAttribute>    newIdentityAttributes;

    @DataModel(IDENTITY_ATTRIBUTES_NAME)
    private List<IdentityAttribute>    identityAttributes;

    @DataModel(OPER_APPLICATION_ALLOWED_DEVICES_NAME)
    private List<DeviceEntry>          allowedDevices;

    @SuppressWarnings("unused")
    @DataModel(value = SELECTED_APPLICATION_USAGE_AGREEMENTS_MODEL)
    private List<UsageAgreementEntity> selectedApplicationUsageAgreements;

    @DataModelSelection(SELECTED_APPLICATION_USAGE_AGREEMENTS_MODEL)
    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private UsageAgreementEntity       operSelectedUsageAgreement;

    @SuppressWarnings("unused")
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity       draftUsageAgreement;

    @SuppressWarnings("unused")
    @Out(required = false, scope = ScopeType.SESSION)
    private UsageAgreementEntity       currentUsageAgreement;


    @Remove
    @Destroy
    public void destroyCallback() {

        name = null;
        friendlyName = null;
        description = null;
        applicationUrl = null;
        applicationLogo = null;
        skipMessageIntegrityCheck = false;
        ssoLogoutUrl = null;
    }


    @SuppressWarnings("unused")
    @DataModel(OPER_APPLICATION_LIST_NAME)
    private List<ApplicationEntity>                 operApplicationList;

    @DataModelSelection(OPER_APPLICATION_LIST_NAME)
    @Out(value = "selectedApplication", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    private ApplicationEntity                       selectedApplication;

    @SuppressWarnings("unused")
    @Out(required = false)
    private String                                  ownerAdminName;

    @SuppressWarnings("unused")
    @DataModel(value = APPLICATION_IDENTITY_ATTRIBUTES_NAME)
    private Set<ApplicationIdentityAttributeEntity> applicationIdentityAttributes;


    @Factory(OPER_APPLICATION_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void applicationListFactory()
            throws ApplicationNotFoundException {

        LOG.debug("application list factory");
        operApplicationList = applicationService.listApplications();
    }

    @Factory(APPLICATION_IDENTITY_ATTRIBUTES_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void applicationIdentityAttributesFactory()
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException {

        LOG.debug("application identity attributes factory");
        long applicationId = selectedApplication.getId();

        applicationIdentityAttributes = applicationService.getCurrentApplicationIdentity(applicationId);
        numberOfSubscriptions = subscriptionService.getNumberOfSubscriptions(applicationId);
        ownerAdminName = subjectService.getSubjectLogin(selectedApplication.getApplicationOwner().getAdmin().getUserId());
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String add()
            throws AttributeTypeNotFoundException, IOException, ApplicationNotFoundException {

        LOG.debug("add application: " + name);
        if (null != friendlyName) {
            LOG.debug("user friendly name: " + friendlyName);
        }
        if (null != applicationUrl) {
            LOG.debug("application url: " + applicationUrl);
        }
        if (null != ssoLogoutUrl) {
            LOG.debug("sso logout url: " + ssoLogoutUrl);
        }

        URL newApplicationUrl = null;
        byte[] newApplicationLogo = null;
        URL newSsoLogoutUrl = null;
        if (null != applicationUrl && applicationUrl.length() != 0) {
            try {
                newApplicationUrl = new URL(applicationUrl);
            } catch (MalformedURLException e) {
                LOG.debug("illegal URL format: " + applicationUrl);
                facesMessages.addToControlFromResourceBundle("applicationUrl", FacesMessage.SEVERITY_ERROR, "errorIllegalUrl",
                        applicationUrl);
                return null;
            }
        }
        if (null != applicationLogoFile) {
            try {
                newApplicationLogo = getUpFileContent(applicationLogoFile);
                if (!Magic.getMagicMatch(newApplicationLogo).getMimeType().startsWith("image/"))
                    throw new MagicException("Application logo requires an image/* MIME type.");
            } catch (IOException e) {
                LOG.debug("couldn't fetch uploaded data for application logo.");
                facesMessages.addToControlFromResourceBundle("applicationLogo", FacesMessage.SEVERITY_ERROR, "errorUploadLogoFetch");
                return null;
            } catch (MagicParseException e) {
                LOG.debug("uploaded logo is not an image.");
                facesMessages.addToControlFromResourceBundle("applicationLogo", FacesMessage.SEVERITY_ERROR, "errorUploadLogoType");
                return null;
            } catch (MagicMatchNotFoundException e) {
                LOG.debug("uploaded logo is not an image.");
                facesMessages.addToControlFromResourceBundle("applicationLogo", FacesMessage.SEVERITY_ERROR, "errorUploadLogoType");
                return null;
            } catch (MagicException e) {
                LOG.debug("uploaded logo is not an image.");
                facesMessages.addToControlFromResourceBundle("applicationLogo", FacesMessage.SEVERITY_ERROR, "errorUploadLogoType");
                return null;
            }
        }
        if (null != ssoLogoutUrl && ssoLogoutUrl.length() != 0) {
            try {
                newSsoLogoutUrl = new URL(ssoLogoutUrl);
            } catch (MalformedURLException e) {
                LOG.debug("illegal URL format: " + ssoLogoutUrl);
                facesMessages.addToControlFromResourceBundle("ssoLogoutUrl", FacesMessage.SEVERITY_ERROR, "errorIllegalUrl", ssoLogoutUrl);
                return null;
            }
        }
        List<IdentityAttributeTypeDO> tempIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
        for (IdentityAttribute viewIdentityAttribute : newIdentityAttributes) {
            if (false == viewIdentityAttribute.isIncluded()) {
                continue;
            }
            LOG.debug("include attribute: " + viewIdentityAttribute.getName());
            IdentityAttributeTypeDO identityAttribute = new IdentityAttributeTypeDO(viewIdentityAttribute.getName(),
                    viewIdentityAttribute.isRequired(), viewIdentityAttribute.isDataMining());
            tempIdentityAttributes.add(identityAttribute);
        }
        try {
            byte[] encodedCertificate;
            if (null != upFile) {
                encodedCertificate = getUpFileContent(upFile);
            } else {
                encodedCertificate = null;
            }
            applicationService.addApplication(name, friendlyName, applicationOwner, description, idmapping,
                    IdScopeType.valueOf(applicationIdScope), newApplicationUrl, newApplicationLogo, encodedCertificate,
                    tempIdentityAttributes, skipMessageIntegrityCheck, deviceRestriction, ssoEnabled, newSsoLogoutUrl);

        } catch (ExistingApplicationException e) {
            LOG.debug("application already exists: " + name);
            facesMessages.addToControlFromResourceBundle("name", FacesMessage.SEVERITY_ERROR, "errorApplicationAlreadyExists", name);
            return null;
        } catch (ApplicationOwnerNotFoundException e) {
            LOG.debug("application owner not found: " + applicationOwner);
            facesMessages.addToControlFromResourceBundle("owner", FacesMessage.SEVERITY_ERROR, "errorApplicationOwnerNotFound",
                    applicationOwner);
            return null;
        } catch (CertificateEncodingException e) {
            LOG.debug("X509 certificate encoding error");
            facesMessages.addToControlFromResourceBundle("fileupload", FacesMessage.SEVERITY_ERROR, "errorX509Encoding");
            return null;
        }

        // fetch new application
        selectedApplication = applicationService.getApplication(name);

        // device restriction
        List<AllowedDeviceEntity> allowedDeviceList = new ArrayList<AllowedDeviceEntity>();
        for (DeviceEntry deviceEntry : allowedDevices) {
            if (deviceEntry.isAllowed() == true) {
                AllowedDeviceEntity device = new AllowedDeviceEntity(selectedApplication, deviceEntry.getDevice(), deviceEntry.getWeight());
                allowedDeviceList.add(device);
            }
        }
        deviceService.setAllowedDevices(selectedApplication, allowedDeviceList);

        applicationListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public UploadedFile getUpFile() {

        return upFile;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setUpFile(UploadedFile uploadedFile) {

        upFile = uploadedFile;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getApplicationUrl() {

        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {

        this.applicationUrl = applicationUrl;
    }

    public byte[] getApplicationLogo() {

        return applicationLogo;
    }

    public void setApplicationLogo(byte[] applicationLogo) {

        this.applicationLogo = applicationLogo;
    }

    public void setApplicationLogoFile(UploadedFile applicationLogoFile) {

        this.applicationLogoFile = applicationLogoFile;
    }

    public UploadedFile getApplicationLogoFile() {

        return applicationLogoFile;
    }

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

    public String getApplicationOwner() {

        return applicationOwner;
    }

    public void setApplicationOwner(String applicationOwner) {

        this.applicationOwner = applicationOwner;
    }

    public boolean isIdmapping() {

        return idmapping;
    }

    public void setIdmapping(boolean idmapping) {

        this.idmapping = idmapping;
    }

    public String getApplicationIdScope() {

        return applicationIdScope;
    }

    public void setApplicationIdScope(String applicationIdScope) {

        this.applicationIdScope = applicationIdScope;
    }

    public boolean isSkipMessageIntegrityCheck() {

        return skipMessageIntegrityCheck;
    }

    public void setSkipMessageIntegrityCheck(boolean skipMessageIntegrityCheck) {

        this.skipMessageIntegrityCheck = skipMessageIntegrityCheck;
    }

    public boolean isDeviceRestriction() {

        return deviceRestriction;
    }

    public void setDeviceRestriction(boolean deviceRestriction) {

        this.deviceRestriction = deviceRestriction;
    }

    public boolean isSsoEnabled() {

        return ssoEnabled;
    }

    public void setSsoEnabled(boolean ssoEnabled) {

        this.ssoEnabled = ssoEnabled;
    }

    public String getSsoLogoutUrl() {

        return ssoLogoutUrl;
    }

    public void setSsoLogoutUrl(String ssoLogoutUrl) {

        this.ssoLogoutUrl = ssoLogoutUrl;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String removeApplication()
            throws ApplicationNotFoundException {

        /*
         * http://jira.jboss.com/jira/browse/EJBTHREE-786
         */
        long applicationId = selectedApplication.getId();
        LOG.debug("remove application: " + applicationId);
        try {
            applicationService.removeApplication(applicationId);
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied to remove: " + applicationId);
            facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), e.getResourceArgs());
            return null;
        }
        applicationListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(NEW_IDENTITY_ATTRIBUTES_NAME)
    public void newIdentityAttributesFactory() {

        newIdentityAttributes = new LinkedList<IdentityAttribute>();
        List<AttributeTypeEntity> attributeTypes = attributeTypeService.listAttributeTypes();
        for (AttributeTypeEntity attributeType : attributeTypes) {
            IdentityAttribute identityAttribute = new IdentityAttribute(attributeType.getName());
            newIdentityAttributes.add(identityAttribute);
        }
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(IDENTITY_ATTRIBUTES_NAME)
    public void identityAttributesFactory()
            throws ApplicationNotFoundException, ApplicationIdentityNotFoundException, PermissionDeniedException {

        Set<ApplicationIdentityAttributeEntity> currentIdentityAttributes = applicationService
                                                                                              .getCurrentApplicationIdentity(selectedApplication
                                                                                                                                                .getId());

        /*
         * Construct a map for fast lookup. The key is the attribute type name.
         */
        Map<String, ApplicationIdentityAttributeEntity> currentIdentity = new HashMap<String, ApplicationIdentityAttributeEntity>();
        for (ApplicationIdentityAttributeEntity applicationIdentityAttribute : currentIdentityAttributes) {
            currentIdentity.put(applicationIdentityAttribute.getAttributeTypeName(), applicationIdentityAttribute);
        }

        /*
         * The view receives a full attribute list, annotated with included and required flags.
         */
        identityAttributes = new LinkedList<IdentityAttribute>();
        List<AttributeTypeEntity> attributeTypes = attributeTypeService.listAttributeTypes();
        for (AttributeTypeEntity attributeType : attributeTypes) {
            boolean included = false;
            boolean required = false;
            boolean dataMining = false;
            ApplicationIdentityAttributeEntity currentIdentityAttribute = currentIdentity.get(attributeType.getName());
            if (null != currentIdentityAttribute) {
                included = true;
                if (currentIdentityAttribute.isRequired()) {
                    required = true;
                }
                if (currentIdentityAttribute.isDataMining()) {
                    dataMining = true;
                }
            }
            IdentityAttribute identityAttribute = new IdentityAttribute(attributeType.getName(), included, required, dataMining);
            identityAttributes.add(identityAttribute);
        }
    }

    private byte[] getUpFileContent(UploadedFile file)
            throws IOException {

        InputStream inputStream = file.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Factory("applicationIdScopes")
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> appliactionIdScopeFactory() {

        List<SelectItem> applicationIdScopes = new LinkedList<SelectItem>();
        for (IdScopeType currentType : IdScopeType.values()) {
            applicationIdScopes.add(new SelectItem(currentType.name(), currentType.name()));
        }
        return applicationIdScopes;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String save()
            throws CertificateEncodingException, ApplicationNotFoundException, IOException, ApplicationIdentityNotFoundException,
            AttributeTypeNotFoundException, PermissionDeniedException {

        long applicationId = selectedApplication.getId();
        LOG.debug("save application: " + applicationId);

        URL newApplicationUrl = null;
        byte[] newApplicationLogo = null;
        URL newSsoLogoutUrl = null;
        if (null != applicationUrl && applicationUrl.length() != 0) {
            try {
                newApplicationUrl = new URL(applicationUrl);
            } catch (MalformedURLException e) {
                LOG.debug("illegal URL format: " + applicationUrl);
                facesMessages.addToControlFromResourceBundle("applicationUrl", FacesMessage.SEVERITY_ERROR, "errorIllegalUrl",
                        applicationUrl);
                return null;
            }
        }
        if (null != applicationLogoFile) {
            try {
                newApplicationLogo = getUpFileContent(applicationLogoFile);
            } catch (IOException e) {
                LOG.debug("couldn't fetch uploaded data for application logo.");
                facesMessages.addToControlFromResourceBundle("applicationLogo", FacesMessage.SEVERITY_ERROR, "errorUploadLogo");
                return null;
            }
        }
        if (null != ssoLogoutUrl && ssoLogoutUrl.length() != 0) {
            try {
                newSsoLogoutUrl = new URL(ssoLogoutUrl);
            } catch (MalformedURLException e) {
                LOG.debug("illegal URL format: " + ssoLogoutUrl);
                facesMessages.addToControlFromResourceBundle("ssoLogoutUrl", FacesMessage.SEVERITY_ERROR, "errorIllegalUrl", ssoLogoutUrl);
                return null;
            }
        }

        if (null != upFile) {
            LOG.debug("updating application certificate");
            applicationService.updateApplicationCertificate(applicationId, getUpFileContent(upFile));
        }

        List<IdentityAttributeTypeDO> tempNewIdentityAttributes = new LinkedList<IdentityAttributeTypeDO>();
        for (IdentityAttribute identityAttribute : identityAttributes) {
            if (false == identityAttribute.isIncluded()) {
                continue;
            }
            IdentityAttributeTypeDO newIdentityAttribute = new IdentityAttributeTypeDO(identityAttribute.getName(),
                    identityAttribute.isRequired(), identityAttribute.isDataMining());
            tempNewIdentityAttributes.add(newIdentityAttribute);
        }

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
        applicationService.updateApplicationIdentity(applicationId, tempNewIdentityAttributes);
        applicationService.updateApplicationUrl(applicationId, newApplicationUrl);
        if (newApplicationLogo != null) {
            applicationService.updateApplicationLogo(applicationId, newApplicationLogo);
        }
        applicationService.setIdentifierMappingServiceAccess(applicationId, idmapping);
        if (null != applicationIdScope) {
            applicationService.setIdScope(applicationId, IdScopeType.valueOf(applicationIdScope));
        }
        applicationService.setSkipMessageIntegrityCheck(applicationId, skipMessageIntegrityCheck);
        applicationService.setSsoEnabled(applicationId, ssoEnabled);
        applicationService.updateSsoLogoutUrl(applicationId, newSsoLogoutUrl);

        // device restriction
        List<AllowedDeviceEntity> allowedDeviceList = new ArrayList<AllowedDeviceEntity>();
        for (DeviceEntry deviceEntry : allowedDevices) {
            if (deviceEntry.isAllowed() == true) {
                AllowedDeviceEntity device = new AllowedDeviceEntity(selectedApplication, deviceEntry.getDevice(), deviceEntry.getWeight());
                allowedDeviceList.add(device);
            }
        }
        applicationService.setApplicationDescription(applicationId, description);
        applicationService.setApplicationDeviceRestriction(applicationId, deviceRestriction);
        deviceService.setAllowedDevices(selectedApplication, allowedDeviceList);

        /*
         * Refresh the selected application.
         */
        selectedApplication = applicationService.getApplication(applicationId);
        applicationIdentityAttributes = applicationService.getCurrentApplicationIdentity(applicationId);

        applicationListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        /*
         * To set the selected application.
         */
        LOG.debug("view application: " + selectedApplication.getName());
        return "view";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String edit() {

        /*
         * To set the selected application.
         */
        LOG.debug("edit application: " + selectedApplication.getName());

        name = selectedApplication.getName();

        friendlyName = selectedApplication.getFriendlyName();

        if (null != selectedApplication.getApplicationUrl()) {
            applicationUrl = selectedApplication.getApplicationUrl().toExternalForm();
        }
        if (null != selectedApplication.getApplicationLogo()) {
            applicationLogo = selectedApplication.getApplicationLogo();
        }
        idmapping = selectedApplication.isIdentifierMappingAllowed();

        skipMessageIntegrityCheck = selectedApplication.isSkipMessageIntegrityCheck();

        applicationIdScope = selectedApplication.getIdScope().name();

        description = selectedApplication.getDescription();

        deviceRestriction = selectedApplication.isDeviceRestriction();

        ssoEnabled = selectedApplication.isSsoEnabled();

        if (null != selectedApplication.getSsoLogoutUrl()) {
            ssoLogoutUrl = selectedApplication.getSsoLogoutUrl().toExternalForm();
        }

        return "edit";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory("availableApplicationOwners")
    public List<SelectItem> availableApplicationOwnersFactory() {

        List<ApplicationOwnerEntity> applicationOwners = applicationService.listApplicationOwners();
        List<SelectItem> availableApplicationOwners = ConvertorUtil.convert(applicationOwners, new ApplicationOwnerSelectItemConvertor());
        return availableApplicationOwners;
    }


    static class ApplicationOwnerSelectItemConvertor implements Convertor<ApplicationOwnerEntity, SelectItem> {

        public SelectItem convert(ApplicationOwnerEntity input) {

            SelectItem output = new SelectItem(input.getName());
            return output;
        }
    }


    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getUsageAgreement()
            throws ApplicationNotFoundException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();

        String text = usageAgreementService.getUsageAgreementText(selectedApplication.getId(), viewLocale.getLanguage());
        if (null == text)
            return "";
        return text;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(OPER_APPLICATION_ALLOWED_DEVICES_NAME)
    public void allowedDevices() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale viewLocale = facesContext.getViewRoot().getLocale();

        allowedDevices = new ArrayList<DeviceEntry>();
        boolean defaultValue = false;

        List<DeviceEntity> deviceList = deviceService.listDevices();
        for (DeviceEntity deviceEntity : deviceList) {
            String deviceDescription = devicePolicyService.getDeviceDescription(deviceEntity.getName(), viewLocale);
            allowedDevices.add(new DeviceEntry(deviceEntity, deviceDescription, defaultValue, 0));
        }

        if (selectedApplication == null)
            return;

        List<AllowedDeviceEntity> allowedDeviceList = deviceService.listAllowedDevices(selectedApplication);

        for (AllowedDeviceEntity allowedDevice : allowedDeviceList) {
            for (DeviceEntry deviceEntry : allowedDevices) {
                if (deviceEntry.getDevice().equals(allowedDevice.getDevice())) {
                    deviceEntry.setAllowed(true);
                    deviceEntry.setWeight(allowedDevice.getWeight());
                }
            }
        }
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    @Factory(SELECTED_APPLICATION_USAGE_AGREEMENTS_MODEL)
    public void usageAgreementListFactory()
            throws ApplicationNotFoundException, PermissionDeniedException {

        if (null == selectedApplication)
            return;
        LOG.debug("usage agreement list factory");
        selectedApplicationUsageAgreements = usageAgreementService.getUsageAgreements(selectedApplication.getId());
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String viewUsageAgreement() {

        LOG.debug("view usage agreement for application: " + selectedApplication.getName() + ", version="
                + operSelectedUsageAgreement.getUsageAgreementVersion());
        return "view-usage-agreement";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String editUsageAgreement()
            throws ApplicationNotFoundException, PermissionDeniedException {

        LOG.debug("edit usage agreement for application: " + selectedApplication.getName());
        draftUsageAgreement = usageAgreementService.getDraftUsageAgreement(selectedApplication.getId());
        currentUsageAgreement = usageAgreementService.getCurrentUsageAgreement(selectedApplication.getId());
        return "edit-usage-agreement";
    }

}
