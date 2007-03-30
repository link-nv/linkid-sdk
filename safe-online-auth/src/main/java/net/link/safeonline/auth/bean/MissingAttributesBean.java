/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.bean;

import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.MissingAttributes;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.IdentityService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("missingAttributes")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX
		+ "MissingAttributesBean/local")
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

	@DataModel("missingAttributeList")
	private List<AttributeDO> missingAttributeList;

	@Factory("missingAttributeList")
	public void missingAttributeListFactory() {
		LOG.debug("missing attribute list factory");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		try {
			this.missingAttributeList = this.identityService
					.getMissingAttributes(this.application, viewLocale);
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

	public String save() {
		LOG.debug("save");
		for (AttributeDO attribute : this.missingAttributeList) {
			String name = attribute.getName();
			String value = attribute.getValue();
			try {
				this.identityService.saveAttribute(name, value);
			} catch (PermissionDeniedException e) {
				String msg = "permission denied for attribute: " + name;
				LOG.debug(msg);
				this.facesMessages.add(msg);
				return null;
			}
		}

		AuthenticationUtils.redirectToApplication(this.target, this.username,
				this.facesMessages);

		return null;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}
}
