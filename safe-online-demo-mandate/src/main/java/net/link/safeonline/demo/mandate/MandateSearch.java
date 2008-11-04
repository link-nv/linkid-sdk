/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate;

import javax.ejb.Local;


@Local
public interface MandateSearch extends AbstractMandateDataClient {

    public static final String JNDI_BINDING = "SafeOnlineMandateDemo/MandateSearchBean/local";


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
