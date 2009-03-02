/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

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
import net.link.safeonline.entity.DatatypeType;
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
import net.link.safeonline.service.ApplicationOwnerAccessControlInterceptor;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.service.DeviceServiceRemote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = DeviceService.JNDI_BINDING)
@RemoteBinding(jndiBinding = DeviceServiceRemote.JNDI_BINDING)
public class DeviceServiceBean implements DeviceService, DeviceServiceRemote {

    private static final Log LOG = LogFactory.getLog(DeviceServiceBean.class);

    @EJB(mappedName = Devices.JNDI_BINDING)
    private Devices          devices;

    @EJB(mappedName = DeviceDAO.JNDI_BINDING)
    private DeviceDAO        deviceDAO;

    @EJB(mappedName = DeviceClassDAO.JNDI_BINDING)
    private DeviceClassDAO   deviceClassDAO;

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO attributeTypeDAO;

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO   applicationDAO;

    @EJB(mappedName = NodeDAO.JNDI_BINDING)
    private NodeDAO          olasDAO;

    @EJB(mappedName = IdentityService.JNDI_BINDING)
    private IdentityService  identityService;


    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.USER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    @Interceptors(ApplicationOwnerAccessControlInterceptor.class)
    public List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application) {

        return devices.listAllowedDevices(application);
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public List<DeviceEntity> listDevices() {

        return devices.listDevices();
    }

    @RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    @Interceptors(ApplicationOwnerAccessControlInterceptor.class)
    public void setAllowedDevices(ApplicationEntity application, List<AllowedDeviceEntity> allowedDeviceList) {

        devices.setAllowedDevices(application, allowedDeviceList);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DeviceClassEntity> listDeviceClasses() {

        return deviceClassDAO.listDeviceClasses();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DeviceDescriptionEntity> listDeviceDescriptions(String deviceName)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        return deviceDAO.listDescriptions(device);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DevicePropertyEntity> listDeviceProperties(String deviceName)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        return deviceDAO.listProperties(device);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceDescription(DeviceDescriptionEntity newDeviceDescription)
            throws DeviceNotFoundException, ExistingDeviceDescriptionException {

        checkExistingDeviceDescription(newDeviceDescription.getDeviceName(), newDeviceDescription.getPk().getLanguage());
        DeviceEntity device = deviceDAO.getDevice(newDeviceDescription.getDeviceName());
        deviceDAO.addDescription(device, newDeviceDescription);
    }

    private void checkExistingDeviceDescription(String deviceName, String language)
            throws ExistingDeviceDescriptionException {

        DeviceDescriptionEntity description = deviceDAO.findDescription(new DeviceDescriptionPK(deviceName, language));
        if (null != description)
            throw new ExistingDeviceDescriptionException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceProperty(DevicePropertyEntity newDeviceProperty)
            throws DeviceNotFoundException, ExistingDevicePropertyException {

        checkExistingDeviceProperty(newDeviceProperty.getDeviceName(), newDeviceProperty.getPk().getName());
        DeviceEntity device = deviceDAO.getDevice(newDeviceProperty.getDeviceName());
        deviceDAO.addProperty(device, newDeviceProperty);
    }

    private void checkExistingDeviceProperty(String deviceName, String name)
            throws ExistingDevicePropertyException {

        DevicePropertyEntity property = deviceDAO.findProperty(new DevicePropertyPK(deviceName, name));
        if (null != property)
            throw new ExistingDevicePropertyException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDevice(String name, String deviceClassName, String nodeName, String authenticationPath, String authenticationWSPath,
                          String registrationPath, String removalPath, String updatePath, String disablePath, String enablePath,
                          String attributeTypeName, String userAttributeTypeName, String disableAttributeTypeName)
            throws DeviceClassNotFoundException, ExistingDeviceException, AttributeTypeNotFoundException, NodeNotFoundException,
            PermissionDeniedException {

        checkExistingDevice(name);
        LOG.debug("add device: " + name);

        DeviceClassEntity deviceClass = deviceClassDAO.getDeviceClass(deviceClassName);
        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeTypeName);
        AttributeTypeEntity userAttributeType;
        if (null == userAttributeTypeName) {
            userAttributeType = null;
        } else {
            userAttributeType = attributeTypeDAO.getAttributeType(userAttributeTypeName);
        }
        checkAttributeTypes(attributeType, userAttributeType);
        AttributeTypeEntity disableAttributeType;
        if (null == disableAttributeTypeName) {
            disableAttributeType = null;
        } else {
            disableAttributeType = attributeTypeDAO.getAttributeType(disableAttributeTypeName);
            if (!disableAttributeType.getType().equals(DatatypeType.BOOLEAN)) {
                String message = "Device disable attribute type should be of type " + DatatypeType.BOOLEAN;
                LOG.debug(message);
                throw new PermissionDeniedException(message);
            }
        }
        checkAttributeTypes(attributeType, disableAttributeType);

        NodeEntity node = olasDAO.getNode(nodeName);

        deviceDAO.addDevice(name, deviceClass, node, authenticationPath, authenticationWSPath, registrationPath, removalPath, updatePath,
                disablePath, enablePath, attributeType, userAttributeType, disableAttributeType);
    }

    /**
     * Check if the user/disable attribute type is or null, or equal to or part of the device attribute type
     * 
     * @throws PermissionDeniedException
     */
    private void checkAttributeTypes(AttributeTypeEntity attributeType, AttributeTypeEntity userOrDisableAttributeType)
            throws PermissionDeniedException {

        if (null == userOrDisableAttributeType)
            return;

        if (attributeType.equals(userOrDisableAttributeType))
            return;

        if (!attributeType.isCompounded()) {
            String message = "Attribute type " + attributeType.getName() + " must be compound and contain user or disable attribute type "
                    + userOrDisableAttributeType + " if both types are not equal";
            LOG.debug("Permission denied: " + message);
            throw new PermissionDeniedException(message);
        }

        List<CompoundedAttributeTypeMemberEntity> memberAttributeTypes = attributeType.getMembers();
        for (CompoundedAttributeTypeMemberEntity memberAttributeType : memberAttributeTypes) {
            if (memberAttributeType.getMember().equals(userOrDisableAttributeType))
                return;
        }

        String message = "Attribute type " + attributeType.getName() + " must be compound and contain user or disable attribute type "
                + userOrDisableAttributeType + " if both types are not equal";
        LOG.debug("Permission denied: " + message);
        throw new PermissionDeniedException(message);
    }

    private void checkExistingDevice(String name)
            throws ExistingDeviceException {

        DeviceEntity existingDevice = deviceDAO.findDevice(name);
        if (null != existingDevice)
            throw new ExistingDeviceException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDevice(String name)
            throws DeviceNotFoundException, DeviceDescriptionNotFoundException, DevicePropertyNotFoundException, PermissionDeniedException {

        DeviceEntity device = deviceDAO.getDevice(name);

        // check if device is in an application's device policy
        List<ApplicationEntity> applications = applicationDAO.listApplications();
        for (ApplicationEntity application : applications) {
            List<AllowedDeviceEntity> allowedDevices = devices.listAllowedDevices(application);
            for (AllowedDeviceEntity allowedDevice : allowedDevices) {
                if (allowedDevice.getDevice().getName().equals(name))
                    throw new PermissionDeniedException("Device still in device policy of " + application.getName(),
                            "errorPermissionDeviceInApplication", application.getName());
            }
        }

        // remove all device descriptions
        List<DeviceDescriptionEntity> deviceDescriptions = deviceDAO.listDescriptions(device);
        for (DeviceDescriptionEntity deviceDescription : deviceDescriptions) {
            removeDeviceDescription(deviceDescription);
        }

        // remove all device properties
        List<DevicePropertyEntity> deviceProperties = deviceDAO.listProperties(device);
        for (DevicePropertyEntity deviceProperty : deviceProperties) {
            removeDeviceProperty(deviceProperty);
        }

        deviceDAO.removeDevice(name);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<DeviceClassDescriptionEntity> listDeviceClassDescriptions(String deviceClassName)
            throws DeviceClassNotFoundException {

        DeviceClassEntity deviceClass = deviceClassDAO.getDeviceClass(deviceClassName);
        return deviceClassDAO.listDescriptions(deviceClass);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceClass(String name, String authenticationContextClass)
            throws ExistingDeviceClassException {

        checkExistingDeviceClass(name);
        LOG.debug("add device class: " + name);

        deviceClassDAO.addDeviceClass(name, authenticationContextClass);
    }

    private void checkExistingDeviceClass(String name)
            throws ExistingDeviceClassException {

        DeviceClassEntity existingDeviceClass = deviceClassDAO.findDeviceClass(name);
        if (null != existingDeviceClass)
            throw new ExistingDeviceClassException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceClass(String name)
            throws PermissionDeniedException {

        checkDeviceClassInUse(name);
        deviceClassDAO.removeDeviceClass(name);
    }

    private void checkDeviceClassInUse(String deviceClassName)
            throws PermissionDeniedException {

        DeviceClassEntity deviceClass = deviceClassDAO.findDeviceClass(deviceClassName);
        List<DeviceEntity> deviceList = deviceDAO.listDevices(deviceClass);
        if (null != deviceList && deviceList.size() > 0)
            throw new PermissionDeniedException("Device class in use by existing devices", "errorPermissionDeviceClassHasDevices");
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void addDeviceClassDescription(DeviceClassDescriptionEntity newDeviceClassDescription)
            throws DeviceClassNotFoundException, ExistingDeviceClassDescriptionException {

        checkExistingDeviceClassDescription(newDeviceClassDescription.getDeviceClassName(), newDeviceClassDescription.getPk().getLanguage());
        DeviceClassEntity deviceClass = deviceClassDAO.getDeviceClass(newDeviceClassDescription.getDeviceClassName());
        deviceClassDAO.addDescription(deviceClass, newDeviceClassDescription);
    }

    private void checkExistingDeviceClassDescription(String deviceClassName, String language)
            throws ExistingDeviceClassDescriptionException {

        LOG.debug("checkExistingDeviceClassDescription: " + deviceClassName + ", " + language);
        DeviceClassDescriptionEntity description = deviceClassDAO.findDescription(new DeviceClassDescriptionPK(deviceClassName, language));
        if (null != description)
            throw new ExistingDeviceClassDescriptionException();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAuthenticationPath(String deviceName, String authenticationPath)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        device.setAuthenticationPath(authenticationPath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAuthenticationWSPath(String deviceName, String authenticationWSPath)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        device.setAuthenticationWSPath(authenticationWSPath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateRegistrationPath(String deviceName, String registrationPath)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        device.setRegistrationPath(registrationPath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateRemovalPath(String deviceName, String removalPath)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        device.setRemovalPath(removalPath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateUpdatePath(String deviceName, String updatePath)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        device.setUpdatePath(updatePath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateDisablePath(String deviceName, String disablePath)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        device.setDisablePath(disablePath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateEnablePath(String deviceName, String enablePath)
            throws DeviceNotFoundException {

        DeviceEntity device = deviceDAO.getDevice(deviceName);
        device.setEnablePath(enablePath);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAttributeType(String deviceName, String attributeTypeName)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException {

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeTypeName);
        DeviceEntity device = deviceDAO.getDevice(deviceName);

        checkAttributeTypes(attributeType, device.getUserAttributeType());
        checkAttributeTypes(attributeType, device.getDisableAttributeType());

        device.setAttributeType(attributeType);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateUserAttributeType(String deviceName, String userAttributeTypeName)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException {

        AttributeTypeEntity userAttributeType = attributeTypeDAO.getAttributeType(userAttributeTypeName);
        DeviceEntity device = deviceDAO.getDevice(deviceName);

        checkAttributeTypes(device.getAttributeType(), userAttributeType);

        device.setUserAttributeType(userAttributeType);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateDisableAttributeType(String deviceName, String disableAttributeTypeName)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException {

        AttributeTypeEntity disableAttributeType = attributeTypeDAO.getAttributeType(disableAttributeTypeName);
        if (!disableAttributeType.getType().equals(DatatypeType.BOOLEAN)) {
            String message = "Device disable attribute type should be of type " + DatatypeType.BOOLEAN;
            LOG.debug(message);
            throw new PermissionDeniedException(message);
        }
        DeviceEntity device = deviceDAO.getDevice(deviceName);

        checkAttributeTypes(device.getAttributeType(), disableAttributeType);

        device.setDisableAttributeType(disableAttributeType);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceClassDescription(DeviceClassDescriptionEntity description)
            throws DeviceClassDescriptionNotFoundException {

        DeviceClassDescriptionEntity deviceClassDescriptionEntity = deviceClassDAO.getDescription(description.getPk());
        deviceClassDAO.removeDescription(deviceClassDescriptionEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDescription(AttributeTypeDescriptionEntity attributeTypeDescription)
            throws AttributeTypeDescriptionNotFoundException {

        AttributeTypeDescriptionEntity attachedEntity = attributeTypeDAO.getDescription(attributeTypeDescription.getPk());
        attributeTypeDAO.removeDescription(attachedEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceDescription(DeviceDescriptionEntity description)
            throws DeviceDescriptionNotFoundException {

        DeviceDescriptionEntity deviceDescriptionEntity = deviceDAO.getDescription(description.getPk());
        deviceDAO.removeDescription(deviceDescriptionEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void removeDeviceProperty(DevicePropertyEntity property)
            throws DevicePropertyNotFoundException {

        DevicePropertyEntity devicePropertyEntity = deviceDAO.getProperty(property.getPk());
        deviceDAO.removeProperty(devicePropertyEntity);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveDeviceClassDescription(DeviceClassDescriptionEntity description) {

        deviceClassDAO.saveDescription(description);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveDeviceDescription(DeviceDescriptionEntity description) {

        deviceDAO.saveDescription(description);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void saveDeviceProperty(DevicePropertyEntity property) {

        deviceDAO.saveProperty(property);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public void updateAuthenticationContextClass(String deviceClassName, String authenticationContextClass)
            throws DeviceClassNotFoundException {

        DeviceClassEntity deviceClass = deviceClassDAO.getDeviceClass(deviceClassName);
        deviceClass.setAuthenticationContextClass(authenticationContextClass);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public DeviceEntity getDevice(String deviceName)
            throws DeviceNotFoundException {

        return deviceDAO.getDevice(deviceName);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public DeviceClassEntity getDeviceClass(String deviceClassName)
            throws DeviceClassNotFoundException {

        return deviceClassDAO.getDeviceClass(deviceClassName);
    }

    @RolesAllowed( { SafeOnlineRoles.USER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
    public List<DeviceRegistrationDO> getDeviceRegistrations(SubjectEntity subject, Locale locale)
            throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException {

        List<DeviceRegistrationDO> deviceRegistrations = new LinkedList<DeviceRegistrationDO>();
        List<DeviceEntity> deviceList = deviceDAO.listDevices();
        for (DeviceEntity device : deviceList) {
            LOG.debug("device: " + device.getName());
            String deviceDescription = device.getName();
            DeviceDescriptionEntity deviceDescriptionEntity = deviceDAO.findDescription(new DeviceDescriptionPK(device.getName(),
                    locale.getLanguage()));
            if (null != deviceDescriptionEntity) {
                deviceDescription = deviceDescriptionEntity.getDescription();
            }
            LOG.debug("device description: " + deviceDescription);

            addRegistrations(deviceRegistrations, subject, device, deviceDescription, locale);
        }
        return deviceRegistrations;
    }

    private void addRegistrations(List<DeviceRegistrationDO> registrations, SubjectEntity subject, DeviceEntity device,
                                  String deviceDescription, Locale locale)
            throws PermissionDeniedException, AttributeTypeNotFoundException {

        List<AttributeDO> attributes;
        try {
            attributes = identityService.listAttributes(subject, device.getAttributeType(), locale);
        } catch (SubjectNotFoundException e) {
            // no registrations found
            return;
        }
        if (null == attributes || attributes.isEmpty())
            return;

        AttributeDO userAttribute = new AttributeDO("empty", DatatypeType.STRING, false, 0, "empty", null, false, false, "", false);
        AttributeDO disableAttribute = null;
        ListIterator<AttributeDO> iter = attributes.listIterator();
        AttributeDO attribute = iter.next();
        while (iter.hasNext()) {
            attribute = iter.next();
            if (null != device.getUserAttributeType() && attribute.getName().equals(device.getUserAttributeType().getName())) {
                userAttribute = attribute;
            } else if (null != device.getDisableAttributeType() && attribute.getName().equals(device.getDisableAttributeType().getName())) {
                disableAttribute = attribute;
            } else if (attribute.getName().equals(device.getAttributeType().getName())) {
                boolean disabled = false;
                if (null != disableAttribute) {
                    disabled = disableAttribute.getBooleanValue();
                }
                registrations.add(new DeviceRegistrationDO(device, deviceDescription, userAttribute, disabled));
            }
        }
        boolean disabled = false;
        if (null != disableAttribute && null != disableAttribute.getBooleanValue()) {
            disabled = disableAttribute.getBooleanValue();
        }
        registrations.add(new DeviceRegistrationDO(device, deviceDescription, userAttribute, disabled));
    }
}
