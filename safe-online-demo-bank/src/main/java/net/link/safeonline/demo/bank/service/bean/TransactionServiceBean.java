/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.service.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.demo.bank.entity.AccountEntity;
import net.link.safeonline.demo.bank.entity.TransactionEntity;
import net.link.safeonline.demo.bank.service.TransactionService;
import net.link.safeonline.demo.bank.service.UserService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link TransactionServiceBean}<br>
 * <sub>Service bean for {@link UserService}.</sub></h2>
 * 
 * <p>
 * <i>Jun 12, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@Attaches(TransactionEntity.class)
@LocalBinding(jndiBinding = UserService.BINDING)
public class TransactionServiceBean extends AbstractBankServiceBean<TransactionEntity> implements TransactionService {

    @EJB
    transient UserService userService;


    /**
     * {@inheritDoc}
     */
    public TransactionEntity createTransaction(String description, AccountEntity source, String target, Double amount) {

        TransactionEntity transaction = new TransactionEntity(description, source, target, new Date(), amount);
        this.em.persist(transaction);

        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<TransactionEntity> getAllTransactions(AccountEntity account) {

        return this.em.createNamedQuery(TransactionEntity.getByCode).setParameter("code", account.getCode())
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    TransactionEntity attachEntity(TransactionEntity transaction) {

        return (TransactionEntity) this.em.createNamedQuery(TransactionEntity.getById).setParameter("id",
                transaction.getId());
    }
}
