/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;


/**
 * <h2>{@link TransactionService}<br>
 * <sub>Service bean for {@link BankUserEntity}.</sub></h2>
 * 
 * <p>
 * Obtain or create {@link BankAccountEntity}s from/for logged in users.
 * </p>
 * 
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Local
public interface TransactionService extends BankService {

    public static final String BINDING = JNDI_PREFIX + "TransactionServiceBean/local";


    /**
     * Create a new account for the given user by the given name.
     * 
     * @return The {@link BankAccountEntity} that was created for the user.
     */
    public BankTransactionEntity createTransaction(String description, BankAccountEntity source, String target, double amount);

    /**
     * Return a sorted collection of transactions made from and to accounts owned by the given user.
     * 
     * The first element in the sorted collection is the most recent transaction.
     */
    public List<BankTransactionEntity> getAllTransactions(BankAccountEntity user);
}
