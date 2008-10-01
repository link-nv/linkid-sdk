/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import javax.ejb.Local;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;


/**
 * <h2>{@link AccountService}<br>
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
public interface AccountService extends BankService {

    public static final String BINDING = JNDI_PREFIX + "AccountServiceBean/local";


    /**
     * Create a new account for the given user by the given name.
     * 
     * @return The {@link BankAccountEntity} that was created for the user.
     */
    public BankAccountEntity createAccount(BankUserEntity user, String name);

    /**
     * @return The account with the given code, or <code>null</code> if no such account is owned by us.
     */
    public BankAccountEntity getAccount(String code);
}
