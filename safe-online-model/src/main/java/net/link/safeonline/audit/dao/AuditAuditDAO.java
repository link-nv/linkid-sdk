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
import net.link.safeonline.entity.audit.AuditAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;


@Local
public interface AuditAuditDAO extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/AuditAuditDAOBean/local";


    void addAuditAudit(AuditContextEntity auditContext, String message);

    void addAuditAudit(String message);

    void cleanup(Long id);

    List<AuditAuditEntity> listRecords(Long id);

    boolean hasRecords(long id);

    List<AuditAuditEntity> listRecordsSince(Date ageLimit);

    List<AuditAuditEntity> listRecords();
}
