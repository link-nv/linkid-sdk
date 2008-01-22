/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.io.IOException;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.service.AuthenticationDevice;
import net.link.safeonline.service.SubjectService;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

/**
 * Abstract login bean. Encapsulates the common code for a Seam backing bean to
 * login a given user.
 * 
 * @author fcorneli
 * 
 */
public class AbstractLoginBean {

	@Logger
	private Log log;

	@EJB
	SubjectService subjectService;

	@SuppressWarnings("unused")
	@Out(value = LoginManager.USERNAME_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
	@In(required = false, scope = ScopeType.SESSION)
	protected String username;

	@SuppressWarnings("unused")
	@Out(value = LoginManager.AUTHENTICATION_DEVICE_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
	@In(required = false, scope = ScopeType.SESSION)
	private AuthenticationDevice authenticationDevice;

	@In(create = true)
	FacesMessages facesMessages;

	protected void clearUsername() {
		this.username = null;
	}

	/**
	 * Login the given user.
	 * 
	 * @param username
	 * @param inputAuthenticationDevice
	 */
	protected void login(String inputUsername, String inputAuthenticationDevice) {
		this.log.debug("login using: " + inputUsername + " via device: "
				+ inputAuthenticationDevice);
		this.username = this.subjectService.findSubjectFromUserName(
				inputUsername).getUserId();
		relogin(inputAuthenticationDevice);
	}

	/**
	 * Re-login the current user. This will trigger the device restriction check
	 * again.
	 * 
	 * @param inputAuthenticationDevice
	 */
	protected void relogin(String inputAuthenticationDevice) {
		this.authenticationDevice = AuthenticationDevice
				.getAuthenticationDevice(inputAuthenticationDevice);
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		String redirectUrl = "../login";
		this.log.debug("redirecting to: " + redirectUrl);
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			this.log.debug("IO error: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorIO");
			return;
		}
	}
}
