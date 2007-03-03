/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.ctrl.bean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.ctrl.LoginBase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;

public class LoginBaseBean implements LoginBase {

	private static final Log LOG = LogFactory.getLog(LoginBaseBean.class);

	@In
	Context sessionContext;

	@In(create = true)
	FacesMessages facesMessages;

	private final String applicationName;

	public LoginBaseBean(String applicationName) {
		this.applicationName = applicationName;
	}

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("post construct: " + this);
	}

	@PreDestroy
	public void preDestroyCallback() {
		LOG.debug("pre destroy: " + this);
	}

	@PostActivate
	public void postActivateCallback() {
		LOG.debug("post activate: " + this);
	}

	@PrePassivate
	public void prePassivateCallback() {
		LOG.debug("pre passivate: " + this);
	}

	public String login() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		String safeOnlineAuthenticationServiceUrl = externalContext
				.getInitParameter("SafeOnlineAuthenticationServiceUrl");
		LOG.debug("redirecting to: " + safeOnlineAuthenticationServiceUrl);
		HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext
				.getRequest();
		String requestUrl = httpServletRequest.getRequestURL().toString();
		String targetUrl = getOverviewTargetUrl(requestUrl);
		LOG.debug("target url: " + targetUrl);
		String redirectUrl;
		try {
			redirectUrl = safeOnlineAuthenticationServiceUrl + "?application="
					+ URLEncoder.encode(this.applicationName, "UTF-8")
					+ "&target=" + URLEncoder.encode(targetUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "UnsupportedEncoding: " + e.getMessage();
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
		try {
			externalContext.redirect(redirectUrl);
		} catch (IOException e) {
			String msg = "IO error: " + e.getMessage();
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		return "login-success";
	}

	public String getOverviewTargetUrl(String requestUrl) {
		int lastSlashIdx = requestUrl.lastIndexOf("/");
		String prefix = requestUrl.substring(0, lastSlashIdx);
		String targetUrl = prefix + "/" + "overview.seam";
		return targetUrl;
	}

	public String logout() {
		LOG.debug("logout");
		this.sessionContext.set("username", null);
		this.sessionContext.set("password", null);
		Seam.invalidateSession();
		return "logout-success";
	}

	public String getLoggedInUsername() {
		LOG.debug("get logged in username");
		String username = (String) this.sessionContext.get("username");
		return username;
	}

	public boolean isLoggedIn() {
		LOG.debug("is logged in");
		String username = (String) this.sessionContext.get("username");
		return (null != username);
	}

	@Remove
	@Destroy
	public void destroyCallback() {
		LOG.debug("destroy: " + this);
	}
}
