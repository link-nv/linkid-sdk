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
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.Timeout;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.service.PublicApplicationService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("timeout")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "TimeoutBean/local")
@Interceptors(ErrorMessageInterceptor.class)
public class TimeoutBean implements Timeout {

	@In(create = true)
	FacesMessages facesMessages;

	@Logger
	private Log log;

	@EJB
	private PublicApplicationService publicApplicationService;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	public String getApplicationUrl() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		try {
			Cookie applicationCookie = (Cookie) facesContext
					.getExternalContext().getRequestCookieMap().get(
							APPLICATION_COOKIE);
			PublicApplication application = this.publicApplicationService
					.findPublicApplication(applicationCookie.getValue());
			if (null != application) {
				if (null != application.getUrl()) {
					log.debug("found url: " + application.getUrl().toString());
					return application.getUrl().toString()
							+ "?authenticationTimeout=true";
				}
			}
			return null;
		} finally {
			this.log.debug("removing entry and timeout cookie");
			HttpServletResponse response = (HttpServletResponse) facesContext
					.getExternalContext().getResponse();
			removeCookie(TIMEOUT_COOKIE, response);
			removeCookie(ENTRY_COOKIE, response);
			removeCookie(APPLICATION_COOKIE, response);
		}
	}

	private void removeCookie(String name, HttpServletResponse response) {
		this.log.debug("remove cookie: " + name);
		Cookie cookie = new Cookie(name, "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}
