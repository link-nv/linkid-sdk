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
import javax.persistence.NoResultException;

import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankTransactionEntity;
import net.link.safeonline.demo.bank.service.AccountService;
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
@LocalBinding(jndiBinding = TransactionService.BINDING)
public class TransactionServiceBean extends AbstractBankServiceBean implements TransactionService {

    @EJB
    transient UserService    userService;

    @EJB
    transient AccountService accountService;


    /**
     * {@inheritDoc}
     */
    public BankTransactionEntity createTransaction(String description, BankAccountEntity source, String targetCode,
            double amount) {

        // Only allow transfers from the source to the target (so only positive amounts).
        if (amount < 0)
            throw new IllegalArgumentException("Can only transfer positive amounts of credit!");
        if (amount == 0)
            throw new IllegalArgumentException("Cannot make a transfer of zero credit.");

        // Deduct the money from the source.
        BankAccountEntity sourceEntity = attach(source);
        sourceEntity.setAmount(sourceEntity.getAmount() - amount);

        // Add the money to the target (if it's a bank account owned by us).
        try {
            BankAccountEntity targetEntity = this.accountService.getAccount(targetCode);
            targetEntity.setAmount(targetEntity.getAmount() + amount);
        } catch (NoResultException e) {
        }

        // Record this transaction.
        BankTransactionEntity transaction = new BankTransactionEntity(description, sourceEntity, targetCode,
                new Date(), amount);
        this.em.persist(transaction);

        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<BankTransactionEntity> getAllTransactions(BankAccountEntity account) {

        return this.em.createNamedQuery(BankTransactionEntity.getByCode)
                .setParameter("code", attach(account).getCode()).getResultList();
    }
}
