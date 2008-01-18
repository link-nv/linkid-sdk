/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service.bean;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceClassNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDescriptionNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DevicePropertyNotFoundException;
import net.link.safeonline.authentication.exception.ExistingDeviceClassException;
import net.link.safeonline.authentication.exception.ExistingDeviceException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.DeviceClassDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.RegisteredDeviceDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.DeviceClassDescriptionEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DevicePropertyEntity;
import net.link.safeonline.entity.RegisteredDeviceEntity;
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
	private Devices devices;

	@EJB
	private DeviceDAO deviceDAO;

	@EJB
	private DeviceClassDAO deviceClassDAO;

	@EJB
	private RegisteredDeviceDAO registeredDeviceDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	@RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.USER_ROLE })
	@Interceptors(ApplicationOwnerAccessControlInterceptor.class)
	public List<AllowedDeviceEntity> listAllowedDevices(
			ApplicationEntity application) {
		return this.devices.listAllowedDevices(application);
	}

	@RolesAllowed( { SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE })
	public List<DeviceEntity> listDevices() {
		return this.devices.listDevices();
	}

	@RolesAllowed(SafeOnlineRoles.OWNER_ROLE)
	@Interceptors(ApplicationOwnerAccessControlInterceptor.class)
	public void setAllowedDevices(ApplicationEntity application,
			List<AllowedDeviceEntity> allowedDeviceList) {
		this.devices.setAllowedDevices(application, allowedDeviceList);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<DeviceClassEntity> listDeviceClasses() {
		return this.deviceClassDAO.listDeviceClasses();
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public List<RegisteredDeviceEntity> listRegisteredDevices(
			SubjectEntity subject) {
		return this.registeredDeviceDAO.listRegisteredDevices(subject);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<DeviceDescriptionEntity> listDeviceDescriptions(
			String deviceName) throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		return this.deviceDAO.listDescriptions(device);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<DevicePropertyEntity> listDeviceProperties(String deviceName)
			throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		return this.deviceDAO.listProperties(device);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addDeviceDescription(
			DeviceDescriptionEntity newDeviceDescription)
			throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(newDeviceDescription
				.getDeviceName());
		this.deviceDAO.addDescription(device, newDeviceDescription);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addDeviceProperty(DevicePropertyEntity newDeviceProperty)
			throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(newDeviceProperty
				.getDeviceName());
		this.deviceDAO.addProperty(device, newDeviceProperty);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addDevice(String name, String deviceClassName,
			String authenticationURL, String registrationURL,
			String removalURL, byte[] encodedCertificate)
			throws CertificateEncodingException, DeviceClassNotFoundException,
			ExistingDeviceException {
		checkExistingDevice(name);
		LOG.debug("add device: " + name);

		X509Certificate certificate = PkiUtils
				.decodeCertificate(encodedCertificate);

		DeviceClassEntity deviceClass = this.deviceClassDAO
				.getDeviceClass(deviceClassName);

		this.deviceDAO.addDevice(name, deviceClass, authenticationURL,
				registrationURL, removalURL, certificate);
	}

	private void checkExistingDevice(String name)
			throws ExistingDeviceException {
		DeviceEntity existingDevice = this.deviceDAO.findDevice(name);
		if (null != existingDevice)
			throw new ExistingDeviceException();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<DeviceClassDescriptionEntity> listDeviceClassDescriptions(
			String deviceClassName) throws DeviceClassNotFoundException {
		DeviceClassEntity deviceClass = this.deviceClassDAO
				.getDeviceClass(deviceClassName);
		return this.deviceClassDAO.listDescriptions(deviceClass);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addDeviceClass(String name) throws ExistingDeviceClassException {
		checkExistingDeviceClass(name);
		LOG.debug("add device class: " + name);

		this.deviceClassDAO.addDeviceClass(name);
	}

	private void checkExistingDeviceClass(String name)
			throws ExistingDeviceClassException {
		DeviceClassEntity existingDeviceClass = this.deviceClassDAO
				.findDeviceClass(name);
		if (null != existingDeviceClass)
			throw new ExistingDeviceClassException();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void addDeviceClassDescription(
			DeviceClassDescriptionEntity newDeviceClassDescription)
			throws DeviceClassNotFoundException {
		DeviceClassEntity deviceClass = this.deviceClassDAO
				.getDeviceClass(newDeviceClassDescription.getDeviceClassName());
		this.deviceClassDAO.addDescription(deviceClass,
				newDeviceClassDescription);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void updateAuthenticationUrl(String deviceName,
			String authenticationURL) throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		device.setAuthenticationURL(authenticationURL);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void updateRegistrationUrl(String deviceName, String registrationURL)
			throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		device.setRegistrationURL(registrationURL);

	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void updateRemovalUrl(String deviceName, String removalURL)
			throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		device.setRemovalURL(removalURL);
	}

	@RolesAllowed(SafeOnlineRoles.USER_ROLE)
	public void updateDeviceCertificate(String deviceName,
			byte[] encodedCertificate) throws DeviceNotFoundException,
			CertificateEncodingException {
		X509Certificate certificate = PkiUtils
				.decodeCertificate(encodedCertificate);

		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		device.setCertificate(certificate);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeDeviceClassDescription(
			DeviceClassDescriptionEntity description)
			throws DeviceClassDescriptionNotFoundException {
		DeviceClassDescriptionEntity deviceClassDescriptionEntity = this.deviceClassDAO
				.getDescription(description.getPk());
		this.deviceClassDAO.removeDescription(deviceClassDescriptionEntity);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeDescription(
			AttributeTypeDescriptionEntity attributeTypeDescription)
			throws AttributeTypeDescriptionNotFoundException {
		AttributeTypeDescriptionEntity attachedEntity = this.attributeTypeDAO
				.getDescription(attributeTypeDescription.getPk());
		this.attributeTypeDAO.removeDescription(attachedEntity);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeDeviceDescription(DeviceDescriptionEntity description)
			throws DeviceDescriptionNotFoundException {
		DeviceDescriptionEntity deviceDescriptionEntity = this.deviceDAO
				.getDescription(description.getPk());
		this.deviceDAO.removeDescription(deviceDescriptionEntity);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void removeDeviceProperty(DevicePropertyEntity property)
			throws DevicePropertyNotFoundException {
		DevicePropertyEntity devicePropertyEntity = this.deviceDAO
				.getProperty(property.getPk());
		this.deviceDAO.removeProperty(devicePropertyEntity);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public void saveDeviceClassDescription(
			DeviceClassDescriptionEntity description) {
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
}
