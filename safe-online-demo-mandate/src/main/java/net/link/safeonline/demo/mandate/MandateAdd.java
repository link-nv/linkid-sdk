/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate;

import javax.ejb.Local;


@Local
public interface MandateAdd extends AbstractMandateDataClient {

    public static final String JNDI_BINDING = "SafeOnlineMandateDemo/MandateAddBean/local";


    /*
     * Actions.
     */
    String add();

    /*
     * Factory
     */
    Mandate newMandateFactory();
}
