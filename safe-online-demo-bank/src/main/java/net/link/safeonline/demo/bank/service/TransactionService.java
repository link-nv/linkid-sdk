/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.demo.bank.entity.AccountEntity;
import net.link.safeonline.demo.bank.entity.TransactionEntity;
import net.link.safeonline.demo.bank.entity.UserEntity;


/**
 * <h2>{@link TransactionService}<br>
 * <sub>Service bean for {@link UserEntity}.</sub></h2>
 * 
 * <p>
 * Obtain or create {@link AccountEntity}s from/for logged in users.
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

    public static final String BINDING = JNDI_PREFIX + "AccountServiceBean/local";


    /**
     * Create a new account for the given user by the given name.
     * 
     * @return The {@link AccountEntity} that was created for the user.
     */
    public TransactionEntity createTransaction(String description, AccountEntity source, String target, Double amount);
    
    /**
     * Return a sorted collection of transactions made from and to accounts owned by the given user.
     * 
     * The first element in the sorted collection is the most recent transaction.
     */
    public List<TransactionEntity> getAllTransactions(AccountEntity user);
}
