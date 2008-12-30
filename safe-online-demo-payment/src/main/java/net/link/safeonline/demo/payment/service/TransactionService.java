/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.PaymentUserEntity;


/**
 * <h2>{@link TransactionService}<br>
 * <sub>Service bean for {@link PaymentUserEntity}.</sub></h2>
 * 
 * <p>
 * Obtain or create {@link PaymentEntity}s from/for logged in users.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface TransactionService extends PaymentService {

    public static final String JNDI_BINDING = JNDI_PREFIX + "TransactionServiceBean/local";


    /**
     * Create a new account for the given user by the given name.
     * 
     * @return The {@link PaymentEntity} that was created for the user.
     */
    public PaymentEntity createTransaction(PaymentUserEntity owner, String visa, String description, String target, double amount);

    /**
     * Return a sorted collection of transactions made from and to accounts owned by the given user.
     * 
     * The first element in the sorted collection is the most recent transaction.
     */
    public List<PaymentEntity> getAllTransactions(PaymentUserEntity user);
}
