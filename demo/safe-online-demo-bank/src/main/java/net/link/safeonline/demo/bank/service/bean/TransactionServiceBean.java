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
@LocalBinding(jndiBinding = TransactionService.JNDI_BINDING)
public class TransactionServiceBean extends AbstractBankServiceBean implements TransactionService {

    @EJB(mappedName = UserService.JNDI_BINDING)
    transient UserService    userService;

    @EJB(mappedName = AccountService.JNDI_BINDING)
    transient AccountService accountService;


    /**
     * {@inheritDoc}
     */
    public BankTransactionEntity createTransaction(String description, BankAccountEntity source, String targetCode, double amount) {

        // Only allow transfers from the source to the target (so only positive amounts).
        if (amount < 0)
            throw new IllegalArgumentException("Can only transfer positive amounts of credit!");
        if (amount == 0)
            throw new IllegalArgumentException("Cannot make a transfer of zero credit.");

        // Deduct the money from the source.
        BankAccountEntity sourceEntity = attach(source);
        sourceEntity.setAmount(sourceEntity.getAmount() - amount);

        // Add the money to the target. (We ignore accounts with external banks in this demo).
        BankAccountEntity targetEntity = accountService.findAccount(targetCode);
        if (targetEntity != null) {
            targetEntity.setAmount(targetEntity.getAmount() + amount);
        }

        // Record this transaction.
        BankTransactionEntity transaction = new BankTransactionEntity(description, sourceEntity, targetCode, new Date(), amount);
        em.persist(transaction);

        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<BankTransactionEntity> getAllTransactions(BankAccountEntity account) {

        return em.createNamedQuery(BankTransactionEntity.getByCode).setParameter("code", attach(account).getCode()).getResultList();
    }
}
