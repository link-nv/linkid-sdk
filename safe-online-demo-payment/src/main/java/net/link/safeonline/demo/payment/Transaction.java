/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.demo.payment.entity.PaymentEntity;


@Local
public interface Transaction extends AbstractPaymentDataClient {

    public static final String JNDI_BINDING = "SafeOnlinePaymentDemo/TransactionBean/local";


    /*
     * Factories.
     */
    PaymentEntity newPaymentEntityFactory();

    List<SelectItem> visasFactory();

    /*
     * Actions.
     */
    String confirm();
}
