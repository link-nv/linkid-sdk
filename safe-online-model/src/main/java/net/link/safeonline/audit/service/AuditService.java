/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Local;

import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;


@Local
public interface AuditService {

    Set<String> listUsers();

    List<AccessAuditEntity> listAccessAuditRecords(String principal);

    List<SecurityAuditEntity> listSecurityAuditRecords(String principal);

    List<AuditContextEntity> listContexts();

    List<AccessAuditEntity> listAccessAuditRecords(Long id);

    boolean removeAuditContext(Long id) throws AuditContextNotFoundException;

    List<SecurityAuditEntity> listSecurityAuditRecords(Long id);

    List<ResourceAuditEntity> listResourceAuditRecords(Long id);

    List<AuditAuditEntity> listAuditAuditRecords(Long id);

    List<AccessAuditEntity> listAccessAuditRecordsSince(Date ageLimit);

    List<SecurityAuditEntity> listSecurityAuditRecordsSince(Date ageLimit);

    List<ResourceAuditEntity> listResourceAuditRecordsSince(Date ageLimit);

    List<AuditAuditEntity> listAuditAuditRecordsSince(Date ageLimit);

    AuditContextEntity getAuditContext(Long id) throws AuditContextNotFoundException;

    List<SecurityAuditEntity> listSecurityAuditRecords();

    List<ResourceAuditEntity> listResourceAuditRecords();

    List<AuditContextEntity> listLastContexts();

    List<AuditAuditEntity> listAuditAuditRecords();

}
