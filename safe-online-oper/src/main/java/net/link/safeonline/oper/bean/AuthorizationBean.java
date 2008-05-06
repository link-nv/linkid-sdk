/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.bean;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.RoleNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.oper.Authorization;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.service.AuthorizationManagerService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("authorizationManager")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "AuthorizationBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Scope(ScopeType.CONVERSATION)
public class AuthorizationBean implements Authorization {

	private String user;

	private Set<String> roles;

	@Logger
	private Log log;

	@EJB
	private AuthorizationManagerService authorizationManagerService;

	@In(create = true)
	FacesMessages facesMessages;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy: " + this);
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Begin
	public String search() {
		this.log.debug("search: #0", this.user);
		try {
			this.roles = this.authorizationManagerService.getRoles(this.user);
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addToControlFromResourceBundle("user",
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return null;
		}
		this.log.debug("roles: #0", this.roles);
		return "found";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String getUser() {
		this.log.debug("get user: #0", this.user);
		return this.user;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setUser(String user) {
		this.user = user;
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String cancel() {
		this.log.debug("cancel");
		this.user = null;
		return "cancel";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String save() {
		this.log.debug("save: " + this.user);
		try {
			this.authorizationManagerService.setRoles(this.user, this.roles);
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorSubjectNotFound");
			return null;
		} catch (RoleNotFoundException e) {
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR, "errorRoleNotFound");
			return null;
		}
		return "saved";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<String> getRoles() {
		this.log.debug("get roles: #0", this.roles);
		return new LinkedList<String>(this.roles);
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public void setRoles(List<String> roles) {
		this.log.debug("set roles: #0", roles);
		this.roles = new HashSet<String>(roles);
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Factory("availableRoles")
	public List<SelectItem> availableRolesFactory() {
		this.log.debug("availableRoles factory");
		Set<String> availableRoles = this.authorizationManagerService
				.getAvailableRoles();
		List<SelectItem> availableRolesView = new LinkedList<SelectItem>();
		for (String availableRole : availableRoles) {
			SelectItem availableRoleView = new SelectItem(availableRole);
			availableRolesView.add(availableRoleView);
		}
		return availableRolesView;
	}

	@PostConstruct
	public void postConstructCallback() {
		this.log.debug("postConstruct: " + this);
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<String> autocompleteUser(Object event) {
		String userPrefix = event.toString();
		if (userPrefix.length() < 3) {
			return null;
		}
		this.log.debug("auto-complete user: #0", userPrefix);
		List<String> filteredUsers;
		try {
			filteredUsers = this.authorizationManagerService
					.getUsers(userPrefix);
		} catch (AttributeTypeNotFoundException e) {
			this.log.debug("login attribute type not found");
			this.facesMessages.addFromResourceBundle(
					FacesMessage.SEVERITY_ERROR,
					"errorAttributeTypeNotFoundSpecific",
					SafeOnlineConstants.LOGIN_ATTRIBTUE);
			return null;
		}
		return filteredUsers;
	}
}
