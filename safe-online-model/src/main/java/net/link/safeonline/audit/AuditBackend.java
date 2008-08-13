/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit;

/**
 * Interface for audit backends.
 *
 * @author fcorneli
 *
 */
public interface AuditBackend {

    public static final String JNDI_CONTEXT = "SafeOnline/audit";

    public static final String JNDI_PREFIX  = JNDI_CONTEXT + "/";


    void process(long auditContextId);
}
