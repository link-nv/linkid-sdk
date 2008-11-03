/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface PaymentServiceEntry extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "/PaymentServiceEntryBean/local";

    /*
     * Actions.
     */
    void init();

    /*
     * Lifecycle methods.
     */
    void destroyCallback();
}
