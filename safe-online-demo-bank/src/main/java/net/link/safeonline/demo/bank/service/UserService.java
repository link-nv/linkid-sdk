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

import net.link.safeonline.demo.bank.entity.AccountEntity;
import net.link.safeonline.demo.bank.entity.UserEntity;


/**
 * <h2>{@link UserService}<br>
 * <sub>Service bean for {@link UserEntity}.</sub></h2>
 *
 * <p>
 * Obtain or create {@link UserEntity}s for logged in users.
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
     * @return The {@link UserEntity} with the given Bank ID, or <code>null</code> if no such ID is known by the bank.
     */
    public UserEntity getBankUser(String bankId);

    /**
     * NOTE: If no {@link UserEntity} with the given OLAS ID exists yet, one will be created and returned.
     * 
     * @return The {@link UserEntity} with the given OLAS ID.
     */
    public UserEntity getOLASUser(String olasId);

    /**
     * Update the given user's attributes from OLAS.
     */
    public UserEntity updateUser(UserEntity user, HttpServletRequest loginRequest);

    /**
     * @return An attached entity for the given one.
     */
    public UserEntity attach(UserEntity user);

    /**
     * @return All accounts that the given user owns.
     */
    public List<AccountEntity> getAccounts(UserEntity user);
}
