/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment;

import javax.ejb.Local;


@Local
public interface PaymentServiceProcess extends AbstractPaymentDataClient {

    /*
     * Actions.
     */
    String authenticate();

    String commit();

    String done();

    /*
     * Accessors.
     */
    String getVisa();

    void setVisa(String visa);

    String getUsername();

}
