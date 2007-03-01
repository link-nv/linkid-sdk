/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.bean;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.model.beid.BeIdConstants;
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

@Stateful
@Name("identity")
@LocalBinding(jndiBinding = UserConstants.JNDI_PREFIX + "IdentityBean/local")
@SecurityDomain(UserConstants.SAFE_ONLINE_USER_SECURITY_DOMAIN)
public class IdentityBean implements Identity {

	private static final Log LOG = LogFactory.getLog(IdentityBean.class);

	@Resource
	private SessionContext context;

	private String name;

	@EJB
	private IdentityService identityService;

	@SuppressWarnings("unused")
	@DataModel
	private List<AttributeDO> attributeList;

	@DataModelSelection
	@Out(required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private AttributeDO selectedAttribute;

	@RolesAllowed(UserConstants.USER_ROLE)
	public String getLogin() {
		Principal principal = this.context.getCallerPrincipal();
		String login = principal.getName();
		return login;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String getName() {
		try {
			this.name = this.identityService
					.findAttributeValue(SafeOnlineConstants.NAME_ATTRIBUTE);
		} catch (PermissionDeniedException e) {
			LOG.error("user not allowed to view attribute: "
					+ SafeOnlineConstants.NAME_ATTRIBUTE);
		}
		LOG.debug("get name: " + this.name);
		return this.name;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public void setName(String name) {
		this.name = name;
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String saveName() {
		LOG.debug("save name");
		try {
			this.identityService.saveAttribute(
					SafeOnlineConstants.NAME_ATTRIBUTE, this.name);
		} catch (PermissionDeniedException e) {
			LOG.error("user not allowed to edit attribute: "
					+ SafeOnlineConstants.NAME_ATTRIBUTE);
			return null;
		}
		this.attributeList = this.identityService.getAttributes();
		return "success";
	}

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	public String getGivenName() {
		try {
			return this.identityService
					.findAttributeValue(BeIdConstants.GIVENNAME_ATTRIBUTE);
		} catch (PermissionDeniedException e) {
			LOG.error("user not allowed to view attribute: "
					+ BeIdConstants.GIVENNAME_ATTRIBUTE);
			return null;
		}
	}

	public String getSurname() {
		try {
			return this.identityService
					.findAttributeValue(BeIdConstants.SURNAME_ATTRIBUTE);
		} catch (PermissionDeniedException e) {
			LOG.error("user not allowed to view attribute: "
					+ BeIdConstants.SURNAME_ATTRIBUTE);
			return null;
		}
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	@Factory("attributeList")
	public void attributeListFactory() {
		LOG.debug("attributeListFactory");
		this.attributeList = this.identityService.getAttributes();
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String edit() {
		LOG.debug("edit attribute: " + this.selectedAttribute.getName());
		return "edit";
	}

	@RolesAllowed(UserConstants.USER_ROLE)
	public String save() {
		String name = this.selectedAttribute.getName();
		LOG.debug("save attribute: " + name);
		String value = this.selectedAttribute.getValue();
		try {
			this.identityService.saveAttribute(name, value);
		} catch (PermissionDeniedException e) {
			LOG.error("user not allowed to edit value for attribute: " + name);
			return null;
		}
		return "success";
	}
}
