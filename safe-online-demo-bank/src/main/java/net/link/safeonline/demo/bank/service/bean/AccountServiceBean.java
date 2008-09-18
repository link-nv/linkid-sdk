/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import java.util.Random;

import javax.ejb.Stateless;

import net.link.safeonline.demo.bank.entity.AccountEntity;
import net.link.safeonline.demo.bank.entity.UserEntity;
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
@LocalBinding(jndiBinding = UserService.BINDING)
public class AccountServiceBean extends AbstractBankServiceBean implements AccountService {

    private static final Random random      = new Random();

    /**
     * {@inheritDoc}
     */
    public AccountEntity createAccount(UserEntity user, String name) {

        String code;
        do {
            code = String.format("%03d-%07d-%02d", random.nextInt(999), random.nextInt(9999999), random.nextInt(99));
        } while (getAccount(code) == null);
        
        AccountEntity account = new AccountEntity(user, name, code);
        this.em.persist(account);
        
        return account;
    }

    /**
     * {@inheritDoc}
     */
    public AccountEntity getAccount(String code) {

        return (AccountEntity) this.em.createNamedQuery(AccountEntity.getByCode).setParameter("code", code)
                .getSingleResult();
    }
}
