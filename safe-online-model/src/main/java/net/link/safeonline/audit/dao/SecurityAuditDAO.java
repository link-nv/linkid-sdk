/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit.dao;

import javax.ejb.Local;

import net.link.safeonline.entity.audit.AuditContextEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;

@Local
public interface SecurityAuditDAO {

	void addSecurityAudit(AuditContextEntity auditContext,
			SecurityThreatType securityThreat, String targetPrincipal,
			String message);

	void cleanup(Long id);
}
