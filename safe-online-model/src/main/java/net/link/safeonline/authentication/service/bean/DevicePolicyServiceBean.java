/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceDescriptionEntity;
import net.link.safeonline.entity.DeviceDescriptionPK;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.model.Devices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Stateless
public class DevicePolicyServiceBean implements DevicePolicyService {

	private static final Log LOG = LogFactory
			.getLog(DevicePolicyServiceBean.class);

	@EJB
	private Devices devices;

	@EJB
	private ApplicationDAO applicationDAO;

	@EJB
	private DeviceDAO deviceDAO;

	public List<DeviceEntity> getDevicePolicy(String applicationId,
			Set<AuthenticationDevice> requiredDevicePolicy)
			throws ApplicationNotFoundException, EmptyDevicePolicyException {
		LOG.debug("get deviec policy for application: " + applicationId);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationId);
		boolean deviceRestriction = application.isDeviceRestriction();
		List<DeviceEntity> devicePolicy = new LinkedList<DeviceEntity>();
		if (deviceRestriction) {
			/*
			 * In this case we use the explicit allowed device list.
			 */
			List<AllowedDeviceEntity> allowedDevices = this.devices
					.listAllowedDevices(application);
			for (AllowedDeviceEntity allowedDevice : allowedDevices) {
				devicePolicy.add(allowedDevice.getDevice());
			}
		} else {
			devicePolicy = this.devices.listDevices();
		}
		if (null != requiredDevicePolicy) {
			for (DeviceEntity device : devicePolicy) {
				boolean found = false;
				for (AuthenticationDevice requiredDevice : requiredDevicePolicy) {
					if (device.getName().equals(requiredDevice.getDeviceName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					devicePolicy.remove(device);
				}
			}
		}
		if (true == devicePolicy.isEmpty()) {
			throw new EmptyDevicePolicyException();
		}
		return devicePolicy;
	}

	public Set<AuthenticationDevice> _getDevicePolicy(String applicationId,
			Set<AuthenticationDevice> requiredDevicePolicy)
			throws ApplicationNotFoundException, EmptyDevicePolicyException {
		LOG.debug("get device policy for application: " + applicationId);
		ApplicationEntity application = this.applicationDAO
				.getApplication(applicationId);
		boolean deviceRestriction = application.isDeviceRestriction();
		Set<AuthenticationDevice> devicePolicy = new HashSet<AuthenticationDevice>();
		if (deviceRestriction) {
			/*
			 * In this case we use the explicit allowed device list.
			 */
			List<AllowedDeviceEntity> allowedDevices = this.devices
					.listAllowedDevices(application);
			for (AllowedDeviceEntity allowedDevice : allowedDevices) {
				String deviceName = allowedDevice.getDevice().getName();
				AuthenticationDevice device = AuthenticationDevice
						.getAuthenticationDevice(deviceName);
				devicePolicy.add(device);
			}
		} else {
			List<DeviceEntity> deviceList = this.devices.listDevices();
			for (DeviceEntity device : deviceList) {
				String deviceName = device.getName();
				AuthenticationDevice authnDevice = AuthenticationDevice
						.getAuthenticationDevice(deviceName);
				devicePolicy.add(authnDevice);
			}
		}
		if (null != requiredDevicePolicy) {
			devicePolicy.retainAll(requiredDevicePolicy);
		}
		if (true == devicePolicy.isEmpty()) {
			throw new EmptyDevicePolicyException();
		}
		return devicePolicy;
	}

	public List<DeviceEntity> getDevices() {
		LOG.debug("get devices");
		return this.devices.listDevices();
	}

	public Set<AuthenticationDevice> _getDevices() {
		LOG.debug("get devices");
		Set<AuthenticationDevice> allDvices = new HashSet<AuthenticationDevice>();
		List<DeviceEntity> deviceList = this.devices.listDevices();
		for (DeviceEntity device : deviceList) {
			String deviceName = device.getName();
			AuthenticationDevice authnDevice = AuthenticationDevice
					.getAuthenticationDevice(deviceName);
			allDvices.add(authnDevice);
		}
		return allDvices;
	}

	public String getDeviceDescription(String deviceName, Locale locale) {
		if (null == locale)
			return deviceName;
		DeviceDescriptionEntity deviceDescription = this.deviceDAO
				.findDescription(new DeviceDescriptionPK(deviceName, locale
						.getLanguage()));
		if (null == deviceDescription)
			return deviceName;
		return deviceDescription.getDescription();
	}

	public String getAuthenticationURL(String deviceName)
			throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		return device.getAuthenticationURL();
	}

	public String getRegistrationURL(String deviceName)
			throws DeviceNotFoundException {
		DeviceEntity device = this.deviceDAO.getDevice(deviceName);
		return device.getRegistrationURL();
	}
}
