/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceClassDescriptionException;
import net.link.safeonline.authentication.exception.ExistingDeviceClassException;
import net.link.safeonline.authentication.exception.ExistingDeviceDescriptionException;
import net.link.safeonline.authentication.exception.ExistingDeviceException;
import net.link.safeonline.authentication.exception.ExistingDevicePropertyException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceClassDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.NodeDAO;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.DeviceRegistrationDO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassDescriptionPK;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceDescriptionPK;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.DevicePropertyPK;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.Devices;
import net.link.safeonline.pkix.PkiUtils;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.service.ApplicationOwnerAccessControlInterceptor;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.service.DeviceServiceRemote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class DeviceServiceBean implements DeviceService, DeviceServiceRemote {

    private static final Log LOG = LogFactory.getLog(DeviceServiceBean.class);

    @EJB
    private Devices          devices;

    @EJB
    private DeviceDAO        deviceDAO;

    @EJB
    private DeviceClassDAO   deviceClassDAO;

    @EJB
    private AttributeTypeDAO attributeTypeDAO;

    @EJB
    private ApplicationDAO   applicationDAO;

    @EJB
    private NodeDAO          olasDAO;

    @EJB
    private IdentityService  identityService;


    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.USER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    @Interceptors(ApplicationOwnerAccessControlInterceptor.class)
    public List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application) {

        return this.devices.listAllowedDevices(application);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public List<DeviceEntity> listDevices() {

        return this.devices.listDevices();
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    @Interceptors(ApplicationOwnerAccessControlInterceptor.class)
    public void setAllowedDevices(ApplicationEntity application, List<AllowedDeviceEntity> allowedDeviceList) {

        this.devices.setAllowedDevices(application, allowedDeviceList);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DeviceClassEntity> listDeviceClasses() {

        return this.deviceClassDAO.listDeviceClasses();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DeviceDescriptionEntity> listDeviceDescriptions(String deviceName) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        return this.deviceDAO.listDescriptions(device);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DevicePropertyEntity> listDeviceProperties(String deviceName) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        return this.deviceDAO.listProperties(device);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceDescription(DeviceDescriptionEntity newDeviceDescription) throws DeviceNotFoundException,
            ExistingDeviceDescriptionException {

        checkExistingDeviceDescription(newDeviceDescription.getDeviceName(), newDeviceDescription.getPk().getLanguage());
        DeviceEntity device = this.deviceDAO.getDevice(newDeviceDescription.getDeviceName());
        this.deviceDAO.addDescription(device, newDeviceDescription);
    }

    private void checkExistingDeviceDescription(String deviceName, String language)
            throws ExistingDeviceDescriptionException {

        DeviceDescriptionEntity description = this.deviceDAO.findDescription(new DeviceDescriptionPK(deviceName,
                language));
        if (null != description)
            throw new ExistingDeviceDescriptionException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceProperty(DevicePropertyEntity newDeviceProperty) throws DeviceNotFoundException,
            ExistingDevicePropertyException {

        checkExistingDeviceProperty(newDeviceProperty.getDeviceName(), newDeviceProperty.getPk().getName());
        DeviceEntity device = this.deviceDAO.getDevice(newDeviceProperty.getDeviceName());
        this.deviceDAO.addProperty(device, newDeviceProperty);
    }

    private void checkExistingDeviceProperty(String deviceName, String name) throws ExistingDevicePropertyException {

        DevicePropertyEntity property = this.deviceDAO.findProperty(new DevicePropertyPK(deviceName, name));
        if (null != property)
            throw new ExistingDevicePropertyException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDevice(String name, String deviceClassName, String nodeName, String authenticationPath,
            String registrationPath, String removalPath, String updatePath, byte[] encodedCertificate,
            String attributeTypeName, String userAttributeTypeName) throws CertificateEncodingException,
            DeviceClassNotFoundException, ExistingDeviceException, AttributeTypeNotFoundException,
            NodeNotFoundException, PermissionDeniedException {

        checkExistingDevice(name);
        LOG.debug("add device: " + name);

        X509Certificate certificate = PkiUtils.decodeCertificate(encodedCertificate);

        DeviceClassEntity deviceClass = this.deviceClassDAO.getDeviceClass(deviceClassName);
        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeTypeName);
        AttributeTypeEntity userAttributeType = this.attributeTypeDAO.getAttributeType(userAttributeTypeName);

        checkAttributeTypes(attributeType, userAttributeType);

        NodeEntity node = this.olasDAO.getNode(nodeName);

        this.deviceDAO.addDevice(name, deviceClass, node, authenticationPath, registrationPath, removalPath,
                updatePath, certificate, attributeType, userAttributeType);
    }

    /**
     * Check if the user attribute type is or equal to or part of the device attribute type
     * 
     * @throws PermissionDeniedException
     */
    private void checkAttributeTypes(AttributeTypeEntity attributeType, AttributeTypeEntity userAttributeType)
            throws PermissionDeniedException {

        if (attributeType.equals(userAttributeType))
            return;

        if (!attributeType.isCompounded()) {
            String message = "Attribute type " + attributeType.getName()
                    + " must be compound and contain user attribute type " + userAttributeType
                    + " if both types are not equal";
            LOG.debug("Permission denied: " + message);
            throw new PermissionDeniedException(message);
        }

        List<CompoundedAttributeTypeMemberEntity> memberAttributeTypes = attributeType.getMembers();
        for (CompoundedAttributeTypeMemberEntity memberAttributeType : memberAttributeTypes) {
            if (memberAttributeType.getMember().equals(userAttributeType))
                return;
        }

        String message = "Attribute type " + attributeType.getName()
                + " must be compound and contain user attribute type " + userAttributeType
                + " if both types are not equal";
        LOG.debug("Permission denied: " + message);
        throw new PermissionDeniedException(message);
    }

    private void checkExistingDevice(String name) throws ExistingDeviceException {

        DeviceEntity existingDevice = this.deviceDAO.findDevice(name);
        if (null != existingDevice)
            throw new ExistingDeviceException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDevice(String name) throws DeviceNotFoundException, DeviceDescriptionNotFoundException,
            DevicePropertyNotFoundException, PermissionDeniedException {

        DeviceEntity device = this.deviceDAO.getDevice(name);

        // check if device is in an application's device policy
        List<ApplicationEntity> applications = this.applicationDAO.listApplications();
        for (ApplicationEntity application : applications) {
            List<AllowedDeviceEntity> allowedDevices = this.devices.listAllowedDevices(application);
            for (AllowedDeviceEntity allowedDevice : allowedDevices) {
                if (allowedDevice.getDevice().getName().equals(name))
                    throw new PermissionDeniedException("Device still in device policy of " + application.getName(),
                            "errorPermissionDeviceInApplication", application.getName());
            }
        }

        // remove all device descriptions
        List<DeviceDescriptionEntity> deviceDescriptions = this.deviceDAO.listDescriptions(device);
        for (DeviceDescriptionEntity deviceDescription : deviceDescriptions) {
            removeDeviceDescription(deviceDescription);
        }

        // remove all device properties
        List<DevicePropertyEntity> deviceProperties = this.deviceDAO.listProperties(device);
        for (DevicePropertyEntity deviceProperty : deviceProperties) {
            removeDeviceProperty(deviceProperty);
        }

        this.deviceDAO.removeDevice(name);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DeviceClassDescriptionEntity> listDeviceClassDescriptions(String deviceClassName)
            throws DeviceClassNotFoundException {

        DeviceClassEntity deviceClass = this.deviceClassDAO.getDeviceClass(deviceClassName);
        return this.deviceClassDAO.listDescriptions(deviceClass);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceClass(String name, String authenticationContextClass) throws ExistingDeviceClassException {

        checkExistingDeviceClass(name);
        LOG.debug("add device class: " + name);

        this.deviceClassDAO.addDeviceClass(name, authenticationContextClass);
    }

    private void checkExistingDeviceClass(String name) throws ExistingDeviceClassException {

        DeviceClassEntity existingDeviceClass = this.deviceClassDAO.findDeviceClass(name);
        if (null != existingDeviceClass)
            throw new ExistingDeviceClassException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceClass(String name) throws PermissionDeniedException {

        checkDeviceClassInUse(name);
        this.deviceClassDAO.removeDeviceClass(name);
    }

    private void checkDeviceClassInUse(String deviceClassName) throws PermissionDeniedException {

        DeviceClassEntity deviceClass = this.deviceClassDAO.findDeviceClass(deviceClassName);
        List<DeviceEntity> deviceList = this.deviceDAO.listDevices(deviceClass);
        if (null != deviceList && deviceList.size() > 0)
            throw new PermissionDeniedException("Device class in use by existing devices",
                    "errorPermissionDeviceClassHasDevices");
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceClassDescription(DeviceClassDescriptionEntity newDeviceClassDescription)
            throws DeviceClassNotFoundException, ExistingDeviceClassDescriptionException {

        checkExistingDeviceClassDescription(newDeviceClassDescription.getDeviceClassName(), newDeviceClassDescription
                .getPk().getLanguage());
        DeviceClassEntity deviceClass = this.deviceClassDAO.getDeviceClass(newDeviceClassDescription
                .getDeviceClassName());
        this.deviceClassDAO.addDescription(deviceClass, newDeviceClassDescription);
    }

    private void checkExistingDeviceClassDescription(String deviceClassName, String language)
            throws ExistingDeviceClassDescriptionException {

        LOG.debug("checkExistingDeviceClassDescription: " + deviceClassName + ", " + language);
        DeviceClassDescriptionEntity description = this.deviceClassDAO.findDescription(new DeviceClassDescriptionPK(
                deviceClassName, language));
        if (null != description)
            throw new ExistingDeviceClassDescriptionException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAuthenticationPath(String deviceName, String authenticationPath) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        device.setAuthenticationPath(authenticationPath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateRegistrationPath(String deviceName, String registrationPath) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        device.setRegistrationPath(registrationPath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateRemovalPath(String deviceName, String removalPath) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        device.setRemovalPath(removalPath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateUpdatePath(String deviceName, String updatePath) throws DeviceNotFoundException {

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        device.setUpdatePath(updatePath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateDeviceCertificate(String deviceName, byte[] encodedCertificate) throws DeviceNotFoundException,
            CertificateEncodingException {

        X509Certificate certificate = PkiUtils.decodeCertificate(encodedCertificate);

        DeviceEntity device = this.deviceDAO.getDevice(deviceName);
        device.setCertificate(certificate);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAttributeType(String deviceName, String attributeTypeName) throws DeviceNotFoundException,
            AttributeTypeNotFoundException, PermissionDeniedException {

        AttributeTypeEntity attributeType = this.attributeTypeDAO.getAttributeType(attributeTypeName);
        DeviceEntity device = this.deviceDAO.getDevice(deviceName);

        checkAttributeTypes(attributeType, device.getUserAttributeType());

        device.setAttributeType(attributeType);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateUserAttributeType(String deviceName, String userAttributeTypeName)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException {

        AttributeTypeEntity userAttributeType = this.attributeTypeDAO.getAttributeType(userAttributeTypeName);
        DeviceEntity device = this.deviceDAO.getDevice(deviceName);

        checkAttributeTypes(device.getAttributeType(), userAttributeType);

        device.setUserAttributeType(userAttributeType);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceClassDescription(DeviceClassDescriptionEntity description)
            throws DeviceClassDescriptionNotFoundException {

        DeviceClassDescriptionEntity deviceClassDescriptionEntity = this.deviceClassDAO.getDescription(description
                .getPk());
        this.deviceClassDAO.removeDescription(deviceClassDescriptionEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDescription(AttributeTypeDescriptionEntity attributeTypeDescription)
            throws AttributeTypeDescriptionNotFoundException {

        AttributeTypeDescriptionEntity attachedEntity = this.attributeTypeDAO.getDescription(attributeTypeDescription
                .getPk());
        this.attributeTypeDAO.removeDescription(attachedEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceDescription(DeviceDescriptionEntity description) throws DeviceDescriptionNotFoundException {

        DeviceDescriptionEntity deviceDescriptionEntity = this.deviceDAO.getDescription(description.getPk());
        this.deviceDAO.removeDescription(deviceDescriptionEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceProperty(DevicePropertyEntity property) throws DevicePropertyNotFoundException {

        DevicePropertyEntity devicePropertyEntity = this.deviceDAO.getProperty(property.getPk());
        this.deviceDAO.removeProperty(devicePropertyEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveDeviceClassDescription(DeviceClassDescriptionEntity description) {

        this.deviceClassDAO.saveDescription(description);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveDeviceDescription(DeviceDescriptionEntity description) {

        this.deviceDAO.saveDescription(description);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveDeviceProperty(DevicePropertyEntity property) {

        this.deviceDAO.saveProperty(property);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAuthenticationContextClass(String deviceClassName, String authenticationContextClass)
            throws DeviceClassNotFoundException {

        DeviceClassEntity deviceClass = this.deviceClassDAO.getDeviceClass(deviceClassName);
        deviceClass.setAuthenticationContextClass(authenticationContextClass);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public DeviceEntity getDevice(String deviceName) throws DeviceNotFoundException {

        return this.deviceDAO.getDevice(deviceName);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public DeviceClassEntity getDeviceClass(String deviceClassName) throws DeviceClassNotFoundException {

        return this.deviceClassDAO.getDeviceClass(deviceClassName);
    }

    @RolesAllowed( { SafeOnlineRoles.USER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public List<DeviceRegistrationDO> getDeviceRegistrations(SubjectEntity subject, Locale locale)
            throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException,
            AttributeTypeNotFoundException {

        List<DeviceRegistrationDO> deviceRegistrations = new LinkedList<DeviceRegistrationDO>();
        List<DeviceEntity> deviceList = this.deviceDAO.listDevices();
        for (DeviceEntity device : deviceList) {
            LOG.debug("device: " + device.getName());
            String deviceDescription = device.getName();
            DeviceDescriptionEntity deviceDescriptionEntity = this.deviceDAO.findDescription(new DeviceDescriptionPK(
                    device.getName(), locale.getLanguage()));
            if (null != deviceDescriptionEntity) {
                deviceDescription = deviceDescriptionEntity.getDescription();
            }
            LOG.debug("device description: " + deviceDescription);
            List<AttributeDO> registrationAttributes = listRegistrations(subject, device, locale);
            if (null == registrationAttributes) {
                continue;
            }
            if (registrationAttributes.isEmpty()) {
                // no user attribute for this device
                deviceRegistrations.add(new DeviceRegistrationDO(device, deviceDescription, null));
            }
            ListIterator<AttributeDO> iter = registrationAttributes.listIterator();
            while (iter.hasNext()) {
                List<AttributeDO> registrationAttributeView = new LinkedList<AttributeDO>();
                AttributeDO registrationAttribute = iter.next();
                if (registrationAttribute.isCompounded()) {
                    registrationAttributeView.add(registrationAttribute);
                    while (iter.hasNext()) {
                        AttributeDO memberAttribute = iter.next();
                        if (memberAttribute.isMember()) {
                            registrationAttributeView.add(memberAttribute);
                        } else {
                            iter.previous();
                            break;
                        }
                    }
                } else {
                    registrationAttributeView.add(registrationAttribute);

                }
                deviceRegistrations.add(new DeviceRegistrationDO(device, deviceDescription, registrationAttributeView));
            }

        }
        return deviceRegistrations;
    }

    private List<AttributeDO> listRegistrations(SubjectEntity subject, DeviceEntity device, Locale locale)
            throws PermissionDeniedException, AttributeTypeNotFoundException {

        if (null == device.getUserAttributeType()) {
            // might be that there still are registrations, check the device attribute type.
            List<AttributeDO> attributes;
            try {
                attributes = this.identityService.listAttributes(subject, device.getAttributeType(), locale);
            } catch (SubjectNotFoundException e) {
                return null;
            }
            if (null != attributes && !attributes.isEmpty())
                return new LinkedList<AttributeDO>();
            return null;
        }
        List<AttributeDO> registeredDeviceAttributes;
        try {
            registeredDeviceAttributes = this.identityService.listAttributes(subject, device.getUserAttributeType(),
                    locale);
        } catch (SubjectNotFoundException e) {
            return null;
        }
        if (null == registeredDeviceAttributes || registeredDeviceAttributes.isEmpty())
            return null;

        return registeredDeviceAttributes;
    }
}
