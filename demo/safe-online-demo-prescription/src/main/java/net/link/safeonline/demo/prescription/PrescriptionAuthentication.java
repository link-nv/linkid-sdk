/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription;

import javax.ejb.Local;


@Local
public interface PrescriptionAuthentication {

    public static final String JNDI_BINDING = "SafeOnlinePrescriptionDemo/PrescriptionAuthenticationBean/local";


    /*
     * Actions.
     */
    String authenticate(String target);
}
