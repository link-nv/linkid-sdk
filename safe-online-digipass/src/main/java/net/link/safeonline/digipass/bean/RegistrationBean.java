/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.digipass.bean;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.digipass.DigipassConstants;
import net.link.safeonline.digipass.Registration;
import net.link.safeonline.model.digipass.DigipassDeviceService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("digipassRegistration")
@LocalBinding(jndiBinding = DigipassConstants.JNDI_PREFIX
		+ "RegistrationBean/local")
public class RegistrationBean implements Registration {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	private String loginName;

	private String serialNumber;

	@EJB
	private DigipassDeviceService digipassDeviceService;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy");
		this.loginName = null;
		this.serialNumber = null;
	}

	public String register() {
		this.log.debug("register digipas with sn=" + this.serialNumber
				+ " for user: " + this.loginName);
		try {
			this.digipassDeviceService.register(this.loginName,
					this.serialNumber);
		} catch (SubjectNotFoundException e) {
			this.log.debug("Subject not found: " + this.loginName);
			this.facesMessages.addToControlFromResourceBundle("login",
					"errorSubjectNotFound");
			return null;
		} catch (ArgumentIntegrityException e) {
			this.log.debug("digipass already registered for: " + this.loginName
					+ "(sn=" + this.serialNumber + ")");
			this.facesMessages.addToControlFromResourceBundle("serialNumber",
					"errorDigipassRegistered");
			return null;
		} catch (PermissionDeniedException e) {
			this.log.debug("Permission denied: " + e.getMessage());
			this.facesMessages.addFromResourceBundle("errorPermissionDenied");
			return null;
		}
		return "success";
	}

	public String getLoginName() {
		return this.loginName;
	}

	public String getSerialNumber() {
		return this.serialNumber;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
