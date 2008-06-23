/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.user.bean;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.RoleNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.ctrl.HistoryMessage;
import net.link.safeonline.ctrl.HistoryMessageManager;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.ctrl.error.annotation.Error;
import net.link.safeonline.ctrl.error.annotation.ErrorHandling;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.data.DeviceMappingDO;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.notification.exception.MessageHandlerNotFoundException;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.user.UserManagement;
import net.link.safeonline.service.AuthorizationManagerService;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.service.SubjectService;

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
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("userManager")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX
		+ "UserManagementBean/local")
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Scope(ScopeType.CONVERSATION)
@Interceptors(ErrorMessageInterceptor.class)
public class UserManagementBean implements UserManagement {

	@Logger
	private Log log;

	private static final String AVAILABLE_ROLES_LIST_NAME = "availableRoles";

	@EJB
	private SubjectService subjectService;

	@EJB
	private AuthorizationManagerService authorizationManagerService;

	@EJB
	private IdentityService identityService;

	@EJB
	private SubscriptionService subscriptionService;

	@EJB
	private DeviceService deviceService;

	@EJB
	private AccountService accountService;

	@In(create = true)
	FacesMessages facesMessages;

	private String user;

	private Set<String> roles;

	private List<HistoryMessage> historyList;

	private List<SubscriptionEntity> subscriptionList;

	private List<DeviceMappingDO> deviceRegistrationList;

	private List<AttributeDO> attributeList;

	@Remove
	@Destroy
	public void destroyCallback() {
		this.log.debug("destroy: " + this);
	}

	@PostConstruct
	public void postConstructCallback() {
		this.log.debug("postConstruct: " + this);
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	@Begin
	@ErrorHandling( { @Error(exceptionClass = SubjectNotFoundException.class, messageId = "errorSubjectNotFound", fieldId = "user") })
	public String search() throws SubjectNotFoundException,
			DeviceNotFoundException, PermissionDeniedException,
			AttributeTypeNotFoundException {
		this.log.debug("search: #0", this.user);
		getUserInfo(this.user);
		return "found";
	}

	private Locale getViewLocale() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Locale viewLocale = facesContext.getViewRoot().getLocale();
		return viewLocale;
	}

	private void getUserInfo(String username) throws SubjectNotFoundException,
			DeviceNotFoundException, PermissionDeniedException,
			AttributeTypeNotFoundException {
		SubjectEntity subject = this.subjectService
				.getSubjectFromUserName(username);

		this.roles = this.authorizationManagerService.getRoles(username);
		this.historyList = getHistoryList(subject);
		this.subscriptionList = this.subscriptionService
				.listSubscriptions(subject);
		this.deviceRegistrationList = this.deviceService
				.getDeviceRegistrations(subject, getViewLocale());
		this.attributeList = this.identityService.listAttributes(subject,
				getViewLocale());
	}

	private List<HistoryMessage> getHistoryList(SubjectEntity subject) {
		List<HistoryEntity> result = this.identityService.listHistory(subject);

		List<HistoryMessage> messageList = new LinkedList<HistoryMessage>();

		for (HistoryEntity historyEntity : result) {
			String historyMessage = HistoryMessageManager.getMessage(
					FacesContext.getCurrentInstance(), historyEntity);
			messageList.add(new HistoryMessage(historyEntity.getWhen(),
					historyMessage));
		}
		return messageList;
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
	public String save() throws SubjectNotFoundException, RoleNotFoundException {
		this.log.debug("save: " + this.user);
		this.authorizationManagerService.setRoles(this.user, this.roles);
		return "saved";
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String remove() {
		this.log.debug("remove: " + this.user);
		return "remove";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String removeConfirm() throws SubjectNotFoundException,
			SubscriptionNotFoundException, MessageHandlerNotFoundException {
		this.log.debug("confirm remove: " + this.user);
		SubjectEntity subject = this.subjectService
				.getSubjectFromUserName(this.user);
		this.accountService.removeAccount(subject);
		return "success";
	}

	@End
	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public String removeCancel() {
		this.log.debug("cancel remove: " + this.user);
		return "success";

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
	@Factory(AVAILABLE_ROLES_LIST_NAME)
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

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<HistoryMessage> getHistoryList() {
		return this.historyList;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<SubscriptionEntity> getSubscriptionList() {
		return this.subscriptionList;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<DeviceMappingDO> getDeviceRegistrationList() {
		return this.deviceRegistrationList;
	}

	@RolesAllowed(OperatorConstants.OPERATOR_ROLE)
	public List<AttributeDO> getAttributeList() {
		return this.attributeList;
	}
}
