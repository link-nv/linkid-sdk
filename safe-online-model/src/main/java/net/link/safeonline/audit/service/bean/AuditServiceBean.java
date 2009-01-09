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
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;
import net.link.safeonline.service.SubjectService;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_SECURITY_DOMAIN)
@LocalBinding(jndiBinding = AuditService.JNDI_BINDING)
public class AuditServiceBean implements AuditService {

    @EJB(mappedName = AuditContextDAO.JNDI_BINDING)
    private AuditContextDAO  auditContextDAO;

    @EJB(mappedName = AuditAuditDAO.JNDI_BINDING)
    private AuditAuditDAO    auditAuditDAO;

    @EJB(mappedName = AccessAuditDAO.JNDI_BINDING)
    private AccessAuditDAO   accessAuditDAO;

    @EJB(mappedName = ResourceAuditDAO.JNDI_BINDING)
    private ResourceAuditDAO resourceAuditDAO;

    @EJB(mappedName = SecurityAuditDAO.JNDI_BINDING)
    private SecurityAuditDAO securityAuditDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService   subjectService;


    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public Set<String> listUsers() {

        List<String> accessUsers = accessAuditDAO.listUsers();
        List<String> securityUsers = securityAuditDAO.listUsers();
        Set<String> users = new HashSet<String>();

        for (String userId : accessUsers) {
            String user = subjectService.getSubjectLogin(userId);
            if (null != user) {
                users.add(user);
            }
        }

        for (String userId : securityUsers) {
            String user = subjectService.getSubjectLogin(userId);
            if (null != user) {
                users.add(user);
            }
        }

        return users;
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AccessAuditEntity> listAccessAuditRecords(String principal) {

        SubjectEntity subject = subjectService.findSubjectFromUserName(principal);
        if (null == subject)
            return null;

        return accessAuditDAO.listRecords(subject.getUserId());
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<SecurityAuditEntity> listSecurityAuditRecords(String principal) {

        SubjectEntity subject = subjectService.findSubjectFromUserName(principal);
        if (null == subject)
            return null;

        return securityAuditDAO.listRecords(subject.getUserId());
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public boolean removeAuditContext(Long id)
            throws AuditContextNotFoundException {

        /*
         * We're not using the cascading of hibernate here.
         */
        securityAuditDAO.cleanup(id);
        accessAuditDAO.cleanup(id);
        auditAuditDAO.cleanup(id);
        resourceAuditDAO.cleanup(id);
        return auditContextDAO.removeAuditContext(id);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AuditContextEntity> listContexts() {

        return auditContextDAO.listContexts();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AccessAuditEntity> listAccessAuditRecords(Long id) {

        return accessAuditDAO.listRecords(id);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AuditAuditEntity> listAuditAuditRecords(Long id) {

        return auditAuditDAO.listRecords(id);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ResourceAuditEntity> listResourceAuditRecords(Long id) {

        return resourceAuditDAO.listRecords(id);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<SecurityAuditEntity> listSecurityAuditRecords(Long id) {

        return securityAuditDAO.listRecords(id);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AccessAuditEntity> listAccessAuditRecordsSince(Date ageLimit) {

        return accessAuditDAO.listRecordsSince(ageLimit);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AuditAuditEntity> listAuditAuditRecordsSince(Date ageLimit) {

        return auditAuditDAO.listRecordsSince(ageLimit);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ResourceAuditEntity> listResourceAuditRecordsSince(Date ageLimit) {

        return resourceAuditDAO.listRecordsSince(ageLimit);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<SecurityAuditEntity> listSecurityAuditRecordsSince(Date ageLimit) {

        return securityAuditDAO.listRecordsSince(ageLimit);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public AuditContextEntity getAuditContext(Long id)
            throws AuditContextNotFoundException {

        return auditContextDAO.getAuditContext(id);
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<ResourceAuditEntity> listResourceAuditRecords() {

        return resourceAuditDAO.listRecords();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<SecurityAuditEntity> listSecurityAuditRecords() {

        return securityAuditDAO.listRecords();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AuditContextEntity> listLastContexts() {

        return auditContextDAO.listLastContexts();
    }

    @RolesAllowed(SafeOnlineRoles.OPERATOR_ROLE)
    public List<AuditAuditEntity> listAuditAuditRecords() {

        return auditAuditDAO.listRecords();
    }
}
