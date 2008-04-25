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
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.MissingAttributes;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.data.AttributeDO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
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
			LOG.debug("application not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorApplicationNotFound");
			return;
		} catch (ApplicationIdentityNotFoundException e) {
			LOG.debug("application identity not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorApplicationIdentityNotFound");
			return;
		} catch (PermissionDeniedException e) {
			LOG.debug("permission denied: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorPermissionDenied");
			return;
		} catch (AttributeTypeNotFoundException e) {
			LOG.debug("attribute type not found.");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			return;
		}
	}

	@RolesAllowed(AuthenticationConstants.USER_ROLE)
	public String save() {
		LOG.debug("save");
		for (AttributeDO attribute : this.missingAttributeList) {
			LOG.debug("attribute to save: " + attribute);
			try {
				this.identityService.saveAttribute(attribute);
			} catch (PermissionDeniedException e) {
				LOG.debug("permission denied for attribute: "
						+ attribute.getName());
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR,
						"errorPermissionDeniedForAttribute", attribute
								.getName());
				return null;
			} catch (AttributeTypeNotFoundException e) {
				LOG.debug("attribute type not found: " + attribute.getName());
				this.facesMessages.addFromResourceBundle(
						FacesMessage.SEVERITY_ERROR,
						"errorAttributeTypeNotFoundSpecific", attribute
								.getName());
				return null;
			}
		}

		AuthenticationUtils.commitAuthentication(this.facesMessages);

		return null;
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}
}
