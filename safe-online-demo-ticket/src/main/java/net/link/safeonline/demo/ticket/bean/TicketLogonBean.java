/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.ticket.bean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.ticket.TicketLogon;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.cache.simple.CacheConfig;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("ticketLogon")
@Scope(ScopeType.SESSION)
@CacheConfig(idleTimeoutSeconds = (5 + 1) * 60)
@LocalBinding(jndiBinding = "SafeOnlineTicketDemo/TicketLogonBean/local")
public class TicketLogonBean implements TicketLogon {

	public static final String APPLICATION_NAME = "safe-online-demo-ticket";

	@Logger
	private Log log;

	@In
	Context sessionContext;

	@In(create = true)
	FacesMessages facesMessages;

	public String login() {
		log.debug("login");
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		String safeOnlineAuthenticationServiceUrl = externalContext
				.getInitParameter("SafeOnlineAuthenticationServiceUrl");
		log.debug("redirecting to #0: ", safeOnlineAuthenticationServiceUrl);
		HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext
				.getRequest();
		String requestUrl = httpServletRequest.getRequestURL().toString();
		String targetUrl = getOverviewTargetUrl(requestUrl);
		log.debug("target url: #0", targetUrl);
		String redirectUrl;
		try {
			redirectUrl = safeOnlineAuthenticationServiceUrl + "?application="
					+ URLEncoder.encode(APPLICATION_NAME, "UTF-8") + "&target="
					+ URLEncoder.encode(targetUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "UnsupportedEncoding: " + e.getMessage();
			log.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			log.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return null;
	}

	public String getOverviewTargetUrl(String requestUrl) {
		int lastSlashIdx = requestUrl.lastIndexOf("/");
		String prefix = requestUrl.substring(0, lastSlashIdx);
		String targetUrl = prefix + "/" + "overview.seam";
		return targetUrl;
	}

	public String logout() {
		log.debug("logout");
		this.sessionContext.set("username", null);
		Seam.invalidateSession();
		return "logout-success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		log.debug("destroy: #0", this);
	}
}
