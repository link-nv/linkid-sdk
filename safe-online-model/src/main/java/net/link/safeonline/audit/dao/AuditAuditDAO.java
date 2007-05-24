/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.audit.AuditContextEntity;

@Local
public interface AuditAuditDAO {

	void addAuditAudit(AuditContextEntity auditContext, String message);

	void addAuditAudit(String message);
}
