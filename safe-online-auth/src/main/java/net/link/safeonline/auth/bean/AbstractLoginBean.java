/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.authentication.service.AuthenticationDevice;

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

	@SuppressWarnings("unused")
	@Out(value = LoginManager.USERNAME_ATTRIBUTE, required = false, scope = ScopeType.SESSION)
	@In(required = false, scope = ScopeType.SESSION)
	private String username;

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
	 * @param authenticationDevice
	 */
	protected void login(String username,
			AuthenticationDevice authenticationDevice) {
		log.debug("login using: " + username + " via device: "
				+ authenticationDevice);
		this.username = username;
		relogin(authenticationDevice);
	}

	/**
	 * Re-login the current user. This will trigger the device restriction check
	 * again.
	 * 
	 * @param authenticationDevice
	 */
	protected void relogin(AuthenticationDevice authenticationDevice) {
		this.authenticationDevice = authenticationDevice;
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		String redirectUrl = "./login";
		log.debug("redirecting to: " + redirectUrl);
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			log.debug(msg);
			this.facesMessages.add(msg);
			return;
		}
	}
}
