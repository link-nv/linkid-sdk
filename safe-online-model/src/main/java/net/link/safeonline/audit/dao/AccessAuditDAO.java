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

import net.link.safeonline.entity.audit.AccessAuditEntity;
import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.OperationStateType;


@Local
public interface AccessAuditDAO {

    void addAccessAudit(AuditContextEntity auditContext, String operation, OperationStateType operationState,
            String principal);

    void cleanup(Long id);

    List<AccessAuditEntity> listRecords(Long id);

    boolean hasErrorRecords(long id);

    List<AccessAuditEntity> listRecords(String principal);

    List<AccessAuditEntity> listRecordsSince(Date ageLimit);

    List<String> listUsers();
}
