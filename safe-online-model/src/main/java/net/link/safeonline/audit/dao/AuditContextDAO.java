/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.dao;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.audit.exception.AuditContextNotFoundException;
import net.link.safeonline.entity.audit.AuditContextEntity;


@Local
public interface AuditContextDAO {

    AuditContextEntity createAuditContext();

    AuditContextEntity getAuditContext(long auditContextId) throws AuditContextNotFoundException;

    void cleanup(long ageInMinutes);

    List<AuditContextEntity> listContexts();

    boolean removeAuditContext(Long id) throws AuditContextNotFoundException;

    List<AuditContextEntity> listLastContexts();
}
