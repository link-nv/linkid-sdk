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

import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.ResourceAuditEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;


@Local
public interface ResourceAuditDAO {

    void addResourceAudit(AuditContextEntity auditContext, ResourceNameType resourceName,
            ResourceLevelType resourceLevel, String sourceComponent, String message);

    void cleanup(Long id);

    List<ResourceAuditEntity> listRecords(Long id);

    boolean hasRecords(long id);

    List<ResourceAuditEntity> listRecordsSince(Date ageLimit);

    List<ResourceAuditEntity> listRecords();

}
