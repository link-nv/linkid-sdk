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
        user = new BankUserEntity("pol", "Pol Van Acker");
        users.add(user);

        accounts.add(account = new BankAccountEntity(user, "persoon", "543-3246784-43"));
        account.setAmount(1200);
        accounts.add(account = new BankAccountEntity(user, "kinderen", "897-2998431-32"));
        account.setAmount(200);
        accounts.add(account = new BankAccountEntity(user, "spaar", "521-9045853-09"));
        account.setAmount(28000);
        
        
        // Persist.
        for (BankUserEntity u : users) {
            this.em.persist(u);
        }
        for (BankAccountEntity a : accounts) {
            this.em.persist(a);
        }
    }
}
