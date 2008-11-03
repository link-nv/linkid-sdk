/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface MandateSearch extends SafeOnlineService, AbstractMandateDataClient {

    /*
     * Accessors.
     */
    String getName();

    void setName(String name);

    /*
     * Actions.
     */
    String search();

    String removeMandate();
}
