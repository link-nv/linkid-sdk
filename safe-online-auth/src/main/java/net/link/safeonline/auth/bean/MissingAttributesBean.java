/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.MissingAttributes;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.IdentityConfirmationRequiredException;
import net.link.safeonline.authentication.exception.MissingAttributeException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.IdentityService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("missingAttributes")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "MissingAttributesBean/local")
@SecurityDomain(AuthenticationConstants.SECURITY_DOMAIN)
public class MissingAttributesBean implements MissingAttributes {

	private static final Log LOG = LogFactory
			.getLog(MissingAttributesBean.class);

	@EJB
	private IdentityService identityService;

	@In(value = "applicationId", required = true)
	private String application;

	@In(create = true)
	FacesMessages facesMessages;

	@In(required = true)
	private String target;

	@In(required = true)
	private String username;

	public static final String MISSING_ATTRIBUTE_LIST = "missingAttributeList";

	@DataModel(MISSING_ATTRIBUTE_LIST)
	private List<AttributeDO> missingAttributeList;

	@Factory(MISSING_ATTRIBUTE_LIST)
	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public void missingAttributeListFactory() {
		LOG.debug("missing attribute list factory");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		try {
			this.missingAttributeList = this.identityService
					.listMissingAttributes(this.application, viewLocale);
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return;
		}
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String save() {
		LOG.debug("save");
		for (AttributeDO attribute : this.missingAttributeList) {
			try {
				this.identityService.saveAttribute(attribute);
			} catch (PermissionDeniedException e) {
				String msg = "permission denied for attribute: "
						+ attribute.getName();
				LOG.debug(msg);
				this.facesMessages.add(msg);
				return null;
			}
		}

		try {
			commitAuthentication();
		} catch (SubscriptionNotFoundException e) {
			String msg = "subscription not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (IdentityConfirmationRequiredException e) {
			String msg = "identity confirmation required";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (MissingAttributeException e) {
			String msg = "missing attribute";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		AuthenticationUtils.redirectToApplication(this.target, this.username,
				this.facesMessages);

		return null;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@In
	private Context sessionContext;

	/**
	 * We're using injection here on the authentication service since it's being
	 * managed by the session-scope based authentication service manager.
	 */
	@In
	private AuthenticationService authenticationService;

	private void commitAuthentication() throws SubscriptionNotFoundException,
			ApplicationNotFoundException, ApplicationIdentityNotFoundException,
			IdentityConfirmationRequiredException, MissingAttributeException {
		try {
			this.authenticationService.commitAuthentication(this.application);
		} finally {
			/*
			 * We have to remove the authentication service reference from the
			 * http session, else the authentication service manager will try to
			 * abort on it.
			 */
			cleanupAuthenticationServiceReference();
		}
	}

	public static final String AUTH_SERVICE_ATTRIBUTE = "authenticationService";

	private void cleanupAuthenticationServiceReference() {
		LOG.debug("cleaning up the authentication service reference");
		this.sessionContext.set(AUTH_SERVICE_ATTRIBUTE, null);
	}
}
