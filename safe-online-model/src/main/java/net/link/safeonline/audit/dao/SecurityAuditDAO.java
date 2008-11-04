/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.SecurityAuditEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;


@Local
public interface SecurityAuditDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/SecurityAuditDAOBean/local";


    void addSecurityAudit(AuditContextEntity auditContext, SecurityThreatType securityThreat, String targetPrincipal, String message);

    void cleanup(Long id);

    List<SecurityAuditEntity> listRecords();

    List<SecurityAuditEntity> listRecords(Long id);

    boolean hasRecords(long id);

    List<SecurityAuditEntity> listRecords(String principal);

    List<SecurityAuditEntity> listRecordsSince(Date ageLimit);

    List<String> listUsers();
}
