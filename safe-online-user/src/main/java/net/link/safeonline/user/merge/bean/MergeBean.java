/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.merge.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.ReAuthenticationService;
import net.link.safeonline.user.UserConstants;
import net.link.safeonline.user.merge.Merge;

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
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.core.ResourceBundle;

@Name("merge")
@Stateful
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "MergeBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class MergeBean implements Merge {

	private static final Log LOG = LogFactory.getLog(MergeBean.class);

	private static final String DEVICE_LIST_NAME = "deviceList";

	@SuppressWarnings("unused")
	@Out(required = false, scope = ScopeType.SESSION)
	@In(required = false, scope = ScopeType.SESSION)
	private String source;

	@Out(required = false, scope = ScopeType.SESSION)
	private AuthenticationDevice deviceSelection;

	@SuppressWarnings("unused")
	@Out(required = false, scope = ScopeType.SESSION)
	@EJB
	private ReAuthenticationService reAuthenticationService;

	@In(create = true)
	FacesMessages facesMessages;

	@EJB
	private DevicePolicyService devicePolicyService;

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy");
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(DEVICE_LIST_NAME)
	public List<SelectItem> deviceListFactory() {
		LOG.debug("deviceListFactory");
		List<SelectItem> allDevices = new LinkedList<SelectItem>();
		Set<AuthenticationDevice> devices = this.devicePolicyService
				.getDevices();
		for (AuthenticationDevice device : devices) {
			String deviceName = device.getDeviceName();
			SelectItem allDevice = new SelectItem(deviceName);
			allDevices.add(allDevice);
		}
		deviceNameDecoration(allDevices);
		return allDevices;

	}

	private void deviceNameDecoration(List<SelectItem> selectItems) {
		for (SelectItem selectItem : selectItems) {
			String deviceId = (String) selectItem.getValue();
			try {
				String deviceName = ResourceBundle.instance().getString(
						deviceId);
				if (null == deviceName) {
					deviceName = deviceId;
				}
				selectItem.setLabel(deviceName);

			} catch (MissingResourceException e) {
				LOG.debug("resource not found: " + deviceId);
			}
		}
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setDeviceSelection(String deviceSelection) {
		this.deviceSelection = AuthenticationDevice
				.getAuthenticationDevice(deviceSelection);
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String getDeviceSelection() {
		if (null == this.deviceSelection)
			return null;
		return this.deviceSelection.getDeviceName();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setSource(String source) {
		this.source = source;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String getSource() {
		return this.source;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String next() {
		LOG.debug("next: " + this.deviceSelection.getDeviceName());
		if (null == this.deviceSelection) {
			LOG.debug("Please make a selection.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorMakeSelection");
			return null;
		}
		return this.deviceSelection.getDeviceName();
	}

}
