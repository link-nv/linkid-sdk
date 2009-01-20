/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import java.util.Random;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.AccountService;
import net.link.safeonline.demo.bank.service.UserService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link AccountServiceBean}<br>
 * <sub>Service bean for {@link UserService}.</sub></h2>
 * 
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = AccountService.JNDI_BINDING)
public class AccountServiceBean extends AbstractBankServiceBean implements AccountService {

    private static final Random random = new Random();


    /**
     * {@inheritDoc}
     */
    public BankAccountEntity createAccount(BankUserEntity user, String name) {

        String code;
        while (true) {
            code = String.format("%03d-%07d-%02d", random.nextInt(999), random.nextInt(9999999), random.nextInt(99));

            try {
                getAccount(code);
            } catch (NoResultException e) {
                break;
            }
        }

        BankAccountEntity account = new BankAccountEntity(attach(user), name, code);
        em.persist(account);

        return account;
    }

    /**
     * {@inheritDoc}
     */
    public BankAccountEntity getAccount(String code) {

        return (BankAccountEntity) em.createNamedQuery(BankAccountEntity.getByCode).setParameter("code", code).getSingleResult();
    }
}
