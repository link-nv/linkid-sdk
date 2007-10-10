/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
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

	public Set<AuthenticationDevice> getDevicePolicy(String applicationId,
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
}
