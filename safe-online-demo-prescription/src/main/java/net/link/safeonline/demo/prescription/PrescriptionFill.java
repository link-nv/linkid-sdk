/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription;

import javax.ejb.Local;


import net.link.safeonline.SafeOnlineService;

@Local
public interface PrescriptionFill extends SafeOnlineService, AbstractPrescriptionDataClient {

    /*
     * Actions.
     */
    String view();

    String fill();

    /*
     * Factories.
     */
    void prescriptionsFactory();
}
