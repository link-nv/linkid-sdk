/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.audit;

import javax.ejb.Local;


@Local
public interface AuditPrincipal {

    /*
     * Accessors
     */
    String getName();

    /*
     * Lifecycle
     */
    void destroyCallback();

}
