/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.audit;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;


@Local
public interface AuditPrincipal {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "AuditPrincipalBean/local";


    /*
     * Accessors
     */
    String getName();

    /*
     * Lifecycle
     */
    void destroyCallback();

}
