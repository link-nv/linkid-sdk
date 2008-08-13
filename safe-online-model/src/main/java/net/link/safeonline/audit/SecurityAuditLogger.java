/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit;

import javax.ejb.Local;

import net.link.safeonline.entity.audit.SecurityThreatType;


@Local
public interface SecurityAuditLogger {

    void addSecurityAudit(SecurityThreatType securityThreat, String message);

    void addSecurityAudit(SecurityThreatType securityThreat, String targetPrincipal, String message);
}
