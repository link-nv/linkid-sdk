/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.IdentityConfirmation;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.entity.AttributeTypeEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;

// TODO: use the user webapp security domain here
@Stateful
@Name("identityConfirmation")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "IdentityConfirmationBean/local")
public class IdentityConfirmationBean implements IdentityConfirmation {

	private static final Log LOG = LogFactory
			.getLog(IdentityConfirmationBean.class);

	@In(value = "applicationId", required = true)
	private String application;

	@In(create = true)
	FacesMessages facesMessages;

	@In(required = true)
	private String target;

	@In(required = true)
	private String username;

	@EJB
	private IdentityService identityService;

	public String agree() {
		LOG.debug("agree");
		boolean hasMissingAttributes;
		try {
			this.identityService.confirmIdentity(this.application);
			hasMissingAttributes = this.identityService
					.hasMissingAttributes(this.application);
		} catch (SubscriptionNotFoundException e) {
			String msg = "subscription not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}

		if (true == hasMissingAttributes) {
			return "missing-attributes";
		}

		AuthenticationUtils.redirectToApplication(this.target, this.username,
				this.facesMessages);

		return null;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@Factory("identityConfirmationList")
	public List<AttributeTypeEntity> identityConfirmationListFactory() {
		LOG.debug("identityConfirmationList factory");
		try {
			List<AttributeTypeEntity> confirmationList = this.identityService
					.getIdentityAttributesToConfirm(this.application);
			return confirmationList;
		} catch (SubscriptionNotFoundException e) {
			String msg = "subscription not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationNotFoundException e) {
			String msg = "application not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		} catch (ApplicationIdentityNotFoundException e) {
			String msg = "application identity not found.";
			LOG.debug(msg);
			this.facesMessages.add(msg);
			return null;
		}
	}
}
