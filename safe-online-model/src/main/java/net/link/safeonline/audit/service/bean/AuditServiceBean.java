/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.service.bean;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.dao.AccessAuditDAO;
import net.link.safeonline.audit.dao.AuditAuditDAO;
import net.link.safeonline.audit.dao.AuditContextDAO;
import net.link.safeonline.audit.dao.ResourceAuditDAO;
import net.link.safeonline.audit.dao.SecurityAuditDAO;
import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.audit.service.AuditService;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;

import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
public class AuditServiceBean implements AuditService {

	@EJB
	private AuditContextDAO auditContextDAO;

	@EJB
	private AuditAuditDAO auditAuditDAO;

	@EJB
	private AccessAuditDAO accessAuditDAO;

	@EJB
	private ResourceAuditDAO resourceAuditDAO;

	@EJB
	private SecurityAuditDAO securityAuditDAO;

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public Set<String> listUsers() {
		List<String> accessUsers = this.accessAuditDAO.listUsers();
		List<String> securityUsers = this.securityAuditDAO.listUsers();
		Set<String> users = new HashSet<String>();
		users.addAll(accessUsers);
		users.addAll(securityUsers);
		return users;
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AccessAuditEntity> listAccessAuditRecords(String principal) {
		return this.accessAuditDAO.listRecords(principal);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<SecurityAuditEntity> listSecurityAuditRecords(String principal) {
		return this.securityAuditDAO.listRecords(principal);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public boolean removeAuditContext(Long id)
			throws AuditContextNotFoundException {
		return this.auditContextDAO.removeAuditContext(id);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AuditContextEntity> listContexts() {
		return this.auditContextDAO.listContexts();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AccessAuditEntity> listAccessAuditRecords(Long id) {
		return this.accessAuditDAO.listRecords(id);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AuditAuditEntity> listAuditAuditRecords(Long id) {
		return this.auditAuditDAO.listRecords(id);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<ResourceAuditEntity> listResourceAuditRecords(Long id) {
		return this.resourceAuditDAO.listRecords(id);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<SecurityAuditEntity> listSecurityAuditRecords(Long id) {
		return this.securityAuditDAO.listRecords(id);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AccessAuditEntity> listAccessAuditRecordsSince(Date ageLimit) {
		return this.accessAuditDAO.listRecordsSince(ageLimit);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AuditAuditEntity> listAuditAuditRecordsSince(Date ageLimit) {
		return this.auditAuditDAO.listRecordsSince(ageLimit);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<ResourceAuditEntity> listResourceAuditRecordsSince(Date ageLimit) {
		return this.resourceAuditDAO.listRecordsSince(ageLimit);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<SecurityAuditEntity> listSecurityAuditRecordsSince(Date ageLimit) {
		return this.securityAuditDAO.listRecordsSince(ageLimit);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public AuditContextEntity getAuditContext(Long id)
			throws AuditContextNotFoundException {
		return this.auditContextDAO.getAuditContext(id);
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<ResourceAuditEntity> listResourceAuditRecords() {
		return this.resourceAuditDAO.listRecords();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<SecurityAuditEntity> listSecurityAuditRecords() {
		return this.securityAuditDAO.listRecords();
	}

	@RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
	public List<AuditContextEntity> listLastContexts() {
		return this.auditContextDAO.listLastContexts();
	}
}
