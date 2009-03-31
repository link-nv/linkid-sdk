/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.service.bean;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.demo.payment.service.TransactionService;
import net.link.safeonline.demo.payment.service.UserService;

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
public class TransactionServiceBean extends AbstractPaymentServiceBean implements TransactionService {

    @EJB(mappedName = UserService.JNDI_BINDING)
    transient UserService userService;


    /**
     * {@inheritDoc}
     */
    public PaymentEntity createTransaction(PaymentUserEntity owner, String visa, String description, String target, double amount) {

        // Only allow transfers from the source to the target (so only positive amounts).
        if (amount < 0)
            throw new IllegalArgumentException("Can only transfer positive amounts of credit!");
        if (amount == 0)
            throw new IllegalArgumentException("Cannot make a transfer of zero credit.");
        // TODO: Test if owner owns visa.

        // Record this transaction.
        PaymentEntity transaction = new PaymentEntity(attach(owner), new Date(), visa, amount, target, description);
        em.persist(transaction);

        return transaction;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<PaymentEntity> getAllTransactions(PaymentUserEntity owner) {

        return em.createNamedQuery(PaymentEntity.getByOwner).setParameter("owner", attach(owner)).getResultList();
    }
}
