/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.Device;

@Stateful
@Name("device")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "DeviceBean/local")
public class DeviceBean implements Device {

	private static final Log LOG = LogFactory.getLog(DeviceBean.class);

	private String selection;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.selection = null;
	}

	public String getSelection() {
		return this.selection;
	}

	public void setSelection(String deviceSelection) {
		this.selection = deviceSelection;
	}

	public String next() {
		LOG.debug("next: " + this.selection);
		if (null == this.selection) {
			String msg = "Please make a selection.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		if ("password".equals(this.selection)) {
			return "/username-password.xhtml";
		}
		if ("beid".equals(this.selection)) {
			return "/beid.xhtml";
		}
		String msg = "Unsupported authentication device: " + this.selection;
		LOG.debug(msg);
		this.facesMessages.add(msg);
		return null;
	}
}
