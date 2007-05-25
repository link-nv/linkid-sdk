/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.IdentityService;
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

	public static final String ATTRIBUTE_LIST_NAME = "attributeList";

	@SuppressWarnings("unused")
	@DataModel(ATTRIBUTE_LIST_NAME)
	private List<AttributeDO> attributeList;

	@DataModelSelection
	@Out(required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private AttributeDO selectedAttribute;

	@In(create = true)
	FacesMessages facesMessages;

	/**
	 * TODO: do better lifecycle management than just pushing everything into
	 * the session.
	 */
	@Out(required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private AttributeDO newAttribute;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory(ATTRIBUTE_LIST_NAME)
	public void attributeListFactory() {
		LOG.debug("attributeListFactory");
		Locale viewLocale = getViewLocale();
		this.attributeList = this.identityService.listAttributes(viewLocale);
	}

	private Locale getViewLocale() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		return viewLocale;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String edit() {
		/*
		 * We have to pass via this bean to let Seam do its job on the data
		 * model selection.
		 */
		LOG.debug("edit attribute: " + this.selectedAttribute.getName());
		return "edit";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String save() {
		String name = this.selectedAttribute.getName();
		LOG.debug("save attribute: " + name);
		try {
			this.identityService.saveAttribute(this.selectedAttribute);
		} catch (PermissionDeniedException e) {
			String msg = "user not allowed to edit value for attribute: "
					+ name;
			LOG.error(msg);
			this.facesMessages.add(msg);
			return null;
		}
		return "success";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String add() {
		LOG.debug("add attribute of type: " + this.selectedAttribute.getName());
		/*
		 * The selectedAttribute serves as a template for the new attribute.
		 * This method should only be invoked for selected multivalued
		 * attributes, since only for multivalued attributes you can add
		 * additional attribute items.
		 */
		Boolean booleanValue = null;
		String stringValue = null;
		boolean multivalued = true;
		boolean dataMining = false;
		boolean editable = true;
		String name = this.selectedAttribute.getName();
		long index = -1; // don't care, core will set it
		String description = this.selectedAttribute.getDescription();
		String type = this.selectedAttribute.getType();
		String humanReadableName = this.selectedAttribute
				.getRawHumanReadableName();
		this.newAttribute = new AttributeDO(name, type, multivalued, index,
				humanReadableName, description, editable, dataMining,
				stringValue, booleanValue);
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
			this.facesMessages.add(msg);
			return null;
		} catch (AttributeNotFoundException e) {
			String msg = "attribute not found";
			LOG.error(msg);
			this.facesMessages.add(msg);
			return null;
		}
		attributeListFactory();
		return "removed";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String commitAdd() {
		LOG.debug("commit add: " + this.newAttribute);
		try {
			this.identityService.addAttribute(this.newAttribute);
		} catch (PermissionDeniedException e) {
			String msg = "user not allowed to add the attribute";
			LOG.error(msg);
			this.facesMessages.add(msg);
			return null;
		}
		attributeListFactory();
		return "success";
	}
}
