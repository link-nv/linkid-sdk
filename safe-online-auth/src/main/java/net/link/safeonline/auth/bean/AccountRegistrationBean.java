/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;

import net.link.safeonline.auth.AccountRegistration;
import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.UserRegistrationService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("accountRegistration")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "AccountRegistrationBean/local")
public class AccountRegistrationBean extends AbstractLoginBean implements
		AccountRegistration {

	@EJB
	private UserRegistrationService userRegistrationService;

	@In
	private AuthenticationService authenticationService;

	@Logger
	private Log log;

	private String login;

	private String device;

	private String password;

	@SuppressWarnings("unused")
	@Out(value = DeviceBean.AUTHN_DEVICE_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
	private String authnDevice;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy");
	}

	@Create
	@Begin
	public void begin() {
		log.debug("begin");
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String loginNext() {
		log.debug("loginNext");

		boolean loginFree = this.userRegistrationService
				.isLoginFree(this.login);
		if (false == loginFree) {
			this.facesMessages.add("login already taken");
			return null;
		}

		return "next";
	}

	public String deviceNext() {
		log.debug("deviceNext");
		if (null == this.device) {
			String msg = "Please make a device selection.";
			this.facesMessages.add(msg);
			return null;
		}
		log.debug("device: " + this.device);
		/*
		 * Next is required for the protocol handler exit point.
		 */
		this.authnDevice = this.device;
		return this.device;
	}

	public String getDevice() {
		return this.device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getPassword() {
		return this.password;
	}

	public String passwordNext() {
		log.debug("passwordNext");

		super.clearUsername();

		try {
			this.userRegistrationService.registerUser(this.login,
					this.password, null);
		} catch (ExistingUserException e) {
			this.facesMessages.add("login already taken");
			return null;
		}

		try {
			boolean authenticated = this.authenticationService.authenticate(
					this.login, this.password);
			if (false == authenticated) {
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR, "authenticationFailedMsg");
				return null;
			}
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addToControlFromResourceBundle("username",
					FacesMessage.SEVERITY_ERROR, "subjectNotFoundMsg");
			return null;
		}

		super.login(this.login);
		return null;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
