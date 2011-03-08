/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.logging.audit;

/**
 * <h2>{@link LinkIDAuditService}<br>
 * <sub>linkID Audit Service API.</sub></h2>
 *
 * <p>
 * linkID Audit Service API. This service should be used if OSGi plugins wish to audit events to linkID's audit system.
 * </p>
 *
 * <p>
 * <i>Sep 18, 2009</i>
 * </p>
 *
 * @author dhouthoo
 */
public interface LinkIDAuditService {

    /**
     * Submit an event to the audit system.
     *
     * @param message What somebody did
     * @param actor Who did it?
     * @param object To who did he do it?
     */
    public void audit(String message, String actor, String object);
}
