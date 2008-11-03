/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface AuditContextFinalizer extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/AuditContextFinalizerBean/local";

    void finalizeAuditContext(Long auditContextId);
}
