/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service;

import java.util.List;

import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;


/**
 * <h2>{@link UserService}<br>
 * <sub>Service bean for {@link BankUserEntity}.</sub></h2>
 *
 * <p>
 * Obtain or create {@link BankUserEntity}s for logged in users.
 * </p>
 *
 * <p>
 * <i>Jan 11, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface UserService extends BankService {

    public static final String BINDING = JNDI_PREFIX + "UserServiceBean/local";


    /**
     * @return The {@link BankUserEntity} with the given Bank ID, or <code>null</code> if no such ID is known by the bank.
     */
    public BankUserEntity getBankUser(String bankId);

    /**
     * NOTE: If no {@link BankUserEntity} with the given OLAS ID exists yet, one will be created and returned.
     * 
     * @return The {@link BankUserEntity} with the given OLAS ID.
     */
    public BankUserEntity getOLASUser(String olasId);

    /**
     * Update the given user's attributes from OLAS.
     */
    public BankUserEntity updateUser(BankUserEntity user, HttpServletRequest loginRequest);

    /**
     * @return All accounts that the given user owns.
     */
    public List<BankAccountEntity> getAccounts(BankUserEntity user);
}
