/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.user.Identity;
import net.link.safeonline.user.UserConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;

@Stateful
@Name("identityBean")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "IdentityBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class IdentityBean implements Identity {

	private static final Log LOG = LogFactory.getLog(IdentityBean.class);

	@EJB
	private IdentityService identityService;

	@EJB
	private SubjectService subjectService;

	public static final String ATTRIBUTE_LIST_NAME = "attributeList";

	@SuppressWarnings("unused")
	@DataModel(ATTRIBUTE_LIST_NAME)
	private List<AttributeDO> attributeList;

	@DataModelSelection(ATTRIBUTE_LIST_NAME)
	@Out(required = false, scope = ScopeType.SESSION)
	private AttributeDO selectedAttribute;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(ATTRIBUTE_LIST_NAME)
	public void attributeListFactory() {
		LOG.debug("attributeListFactory");
		Locale viewLocale = getViewLocale();
		try {
			this.attributeList = this.identityService
					.listAttributes(viewLocale);
		} catch (AttributeTypeNotFoundException e) {
			LOG.error("attribute type not found: " + e.getMessage());
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorAttributeTypeNotFoundSpecific", e.getMessage());
			this.attributeList = new LinkedList<AttributeDO>();
		}
	}

	private Locale getViewLocale() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		return viewLocale;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String edit() {
		LOG.debug("edit attribute: " + this.selectedAttribute.getName());
		return "edit";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String add() {
		LOG.debug("add attribute of type: " + this.selectedAttribute.getName());
		return "add";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String removeAttribute() {
		LOG.debug("remove attribute: " + this.selectedAttribute);
		try {
			this.identityService.removeAttribute(this.selectedAttribute);
		} catch (PermissionDeniedException e) {
			String msg = "user not allowed to remove the attribute";
			LOG.error(msg);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorUserNotAllowedToRemoveAttribute");
			return null;
		} catch (AttributeNotFoundException e) {
			String msg = "attribute not found";
			LOG.error(msg);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeNotFound");
			return null;
		} catch (AttributeTypeNotFoundException e) {
			String msg = "attribute type not found";
			LOG.error(msg);
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorAttributeTypeNotFound");
			return null;
		}
		attributeListFactory();
		return "removed";
	}

	public String getUsername() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		String userId = externalContext.getUserPrincipal().getName();
		String username = this.subjectService.getSubjectLogin(userId);
		return username;
	}
}
