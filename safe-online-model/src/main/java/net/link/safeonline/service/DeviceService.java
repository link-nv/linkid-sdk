package net.link.safeonline.service;

import java.util.List;
import java.util.Locale;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
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
import net.link.safeonline.data.DeviceRegistrationDO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.pkix.exception.CertificateEncodingException;


@Local
public interface DeviceService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/DeviceServiceBean/local";


    List<DeviceEntity> listDevices();

    List<AllowedDeviceEntity> listAllowedDevices(ApplicationEntity application);

    void setAllowedDevices(ApplicationEntity application, List<AllowedDeviceEntity> allowedDeviceList);

    List<DeviceClassEntity> listDeviceClasses();

    List<DeviceDescriptionEntity> listDeviceDescriptions(String deviceName)
            throws DeviceNotFoundException;

    List<DevicePropertyEntity> listDeviceProperties(String deviceName)
            throws DeviceNotFoundException;

    void addDeviceDescription(DeviceDescriptionEntity newDeviceDescription)
            throws DeviceNotFoundException, ExistingDeviceDescriptionException;

    void removeDeviceDescription(DeviceDescriptionEntity description)
            throws DeviceDescriptionNotFoundException;

    void addDeviceProperty(DevicePropertyEntity newDeviceProperty)
            throws DeviceNotFoundException, ExistingDevicePropertyException;

    void removeDeviceProperty(DevicePropertyEntity property)
            throws DevicePropertyNotFoundException;

    void addDevice(String name, String deviceClassName, String nodeName, String authenticationPath, String registrationPath,
                   String removalPath, String updatePath, String disablePath, byte[] encodedCertificate, String attributeTypeName,
                   String userAttributeTypeName, String disableAttributeTypeName)
            throws CertificateEncodingException, DeviceClassNotFoundException, ExistingDeviceException, AttributeTypeNotFoundException,
            NodeNotFoundException, PermissionDeniedException;

    void removeDevice(String name)
            throws DeviceNotFoundException, DeviceDescriptionNotFoundException, DevicePropertyNotFoundException, PermissionDeniedException;

    List<DeviceClassDescriptionEntity> listDeviceClassDescriptions(String deviceClassName)
            throws DeviceClassNotFoundException;

    void addDeviceClassDescription(DeviceClassDescriptionEntity newDeviceClassDescription)
            throws DeviceClassNotFoundException, ExistingDeviceClassDescriptionException;

    void removeDeviceClassDescription(DeviceClassDescriptionEntity description)
            throws DeviceClassDescriptionNotFoundException;

    void addDeviceClass(String name, String authenticationContextClass)
            throws ExistingDeviceClassException;

    void removeDeviceClass(String name)
            throws PermissionDeniedException;

    void updateAuthenticationPath(String deviceName, String authenticationPath)
            throws DeviceNotFoundException;

    void updateRegistrationPath(String deviceName, String registrationPath)
            throws DeviceNotFoundException;

    void updateRemovalPath(String deviceName, String removalPath)
            throws DeviceNotFoundException;

    void updateUpdatePath(String deviceName, String updatePath)
            throws DeviceNotFoundException;

    void updateDisablePath(String deviceName, String disablePath)
            throws DeviceNotFoundException;

    void updateDeviceCertificate(String deviceName, byte[] encodedCertificate)
            throws DeviceNotFoundException, CertificateEncodingException;

    void saveDeviceDescription(DeviceDescriptionEntity description);

    void saveDeviceProperty(DevicePropertyEntity property);

    void saveDeviceClassDescription(DeviceClassDescriptionEntity description);

    void updateAuthenticationContextClass(String deviceClassName, String authenticationContextClass)
            throws DeviceClassNotFoundException;

    DeviceEntity getDevice(String deviceName)
            throws DeviceNotFoundException;

    DeviceClassEntity getDeviceClass(String deviceClassName)
            throws DeviceClassNotFoundException;

    void updateAttributeType(String deviceName, String attributeType)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException;

    void updateUserAttributeType(String deviceName, String userAttributeType)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException;

    void updateDisableAttributeType(String deviceName, String disableAttributeType)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException;

    /**
     * Returns the list of device registrations for the specified subject.
     * 
     * @param subject
     * @param locale
     * @return list of device registration data objects.
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     * @throws DeviceNotFoundException
     * @throws SubjectNotFoundException
     */
    List<DeviceRegistrationDO> getDeviceRegistrations(SubjectEntity subject, Locale locale)
            throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException;

}
