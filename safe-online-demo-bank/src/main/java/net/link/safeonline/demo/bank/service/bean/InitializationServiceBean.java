/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.InitializationService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link InitializationServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 22, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
@Stateless
@LocalBinding(jndiBinding = InitializationService.BINDING)
public class InitializationServiceBean extends AbstractBankServiceBean implements InitializationService {

    /**
     * {@inheritDoc}
     */
    public void buildEntities() {

        // Containers.
        List<BankUserEntity> users = new LinkedList<BankUserEntity>();
        List<BankAccountEntity> accounts = new LinkedList<BankAccountEntity>();
        BankUserEntity user;
        BankAccountEntity account;

        // Pol.
        user = new BankUserEntity(digipassUser_BankId, digipassUser_Name);
        users.add(user);

        for (int i = 0; i < digipassUser_AccountNames.length; ++i) {
            accounts.add(account = new BankAccountEntity(user, digipassUser_AccountNames[i], digipassUser_AccountCodes[i]));
            account.setAmount(digipassUser_AccountAmounts[i]);
        }

        // Persist.
        for (BankUserEntity u : users) {
            this.em.persist(u);
        }
        for (BankAccountEntity a : accounts) {
            this.em.persist(a);
        }
    }
}
