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

    public static final String JNDI_BINDING = JNDI_PREFIX + "UserServiceBean/local";


    /**
     * @return A list of all known {@link BankUserEntity}s.
     */
    public List<BankUserEntity> getUsers();

    /**
     * Create a new user with the given bank ID, and the given username. This user has no OLAS account. To create users with an OLAS
     * account, see {@link #getOLASUser(String)}.
     * 
     * @return The {@link BankUserEntity} that was created.
     */
    public BankUserEntity addUser(String bankId, String userName);

    /**
     * Remove the given bank user entity permanently.
     */
    public void removeUser(BankUserEntity user);

    /**
     * @return The {@link BankUserEntity} with the given Bank ID.
     */
    public BankUserEntity getBankUser(String bankId);

    /**
     * @return The {@link BankUserEntity} with the given Bank ID, or <code>null</code> if no such ID is known by the bank.
     */
    public BankUserEntity findBankUser(String bankId);

    /**
     * NOTE: If no {@link BankUserEntity} with the given OLAS ID exists yet, one will be created and returned.
     * 
     * @return The {@link BankUserEntity} with the given OLAS ID.
     */
    public BankUserEntity getOLASUser(String olasId);

    /**
     * @return The {@link BankUserEntity} with the given OLAS ID or <code>null</code> if no bank user has the given OLAS ID.
     */
    public BankUserEntity findOLASUser(String olasId);

    /**
     * @return The {@link BankUserEntity} that has been linked to the OLAS account of the given OLAS id.
     */
    public BankUserEntity linkOLASUser(BankUserEntity user, String olasId);

    /**
     * Remove the OLAS userId from the given user entity so that it is no longer linked to an OLAS account.
     */
    public void unlinkOLASUser(BankUserEntity user);

    /**
     * Update the given user's attributes from OLAS.
     */
    public BankUserEntity updateUser(BankUserEntity user);

    /**
     * @return All accounts that the given user owns.
     */
    public List<BankAccountEntity> getAccounts(BankUserEntity user);
}
