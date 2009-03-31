/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.model.bean;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.demo.bank.entity.BankAccountEntity;
import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.service.InitializationService;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.demo.cinema.service.TicketService;
import net.link.safeonline.demo.model.NotificationConsumerService;
import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.PaymentUserEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = NotificationConsumerService.JNDI_BINDING)
public class NotificationConsumerServiceBean implements NotificationConsumerService {

    private static final Log    LOG                           = LogFactory.getLog(NotificationConsumerServiceBean.class);

    private static final String DEMO_BANK_APPLICATION_NAME    = "ebank";
    private static final String DEMO_CINEMA_APPLICATION_NAME  = "cinema";
    private static final String DEMO_PAYMENT_APPLICATION_NAME = "epayment";

    private EntityManager       demoBankEntityManager;
    private EntityManager       demoCinemaEntityManager;
    private EntityManager       demoPaymentEntityManager;

    private TicketService       demoCinemaTicketService;


    public void handleMessage(String topic, String destination, String subject, String content) {

        try {
            InitialContext context = new InitialContext();
            demoBankEntityManager = (EntityManager) context.lookup("java:/DemoBankEntityManager");
            demoCinemaEntityManager = (EntityManager) context.lookup("java:/DemoCinemaEntityManager");
            demoPaymentEntityManager = (EntityManager) context.lookup("java:/DemoPaymentEntityManager");
            demoCinemaTicketService = (TicketService) context.lookup(TicketService.JNDI_BINDING);
        } catch (NamingException e) {
            LOG.error("Naming exception thrown: " + e.getMessage(), e);
        }

        if (topic.equals(SafeOnlineConstants.TOPIC_REMOVE_USER)) {
            if (destination.equals(DEMO_BANK_APPLICATION_NAME)) {
                removeDemoBankUser(subject);
            } else if (destination.equals(DEMO_CINEMA_APPLICATION_NAME)) {
                removeDemoCinemaUser(subject);
            } else if (destination.equals(DEMO_PAYMENT_APPLICATION_NAME)) {
                removeDemoPaymentUser(subject);
            }
        } else if (topic.equals(SafeOnlineConstants.TOPIC_UNSUBSCRIBE_USER)) {
            if (destination.equals(DEMO_BANK_APPLICATION_NAME)) {
                unsubscribeDemoBankUser(subject);
            }
        }
    }

    private void unsubscribeDemoBankUser(String userId) {

        LOG.debug("unsubscribe demo bank user id: " + userId);

        try {
            BankUserEntity user = (BankUserEntity) demoBankEntityManager.createNamedQuery(BankUserEntity.getByOlasId).setParameter(
                    "olasId", userId).getSingleResult();
            user.setOlasId(null);
            user.setName(InitializationService.digipassUser_Name);
        } catch (NoResultException e) {
        }
    }

    private void removeDemoBankUser(String userId) {

        LOG.debug("remove demo bank user id: " + userId);

        try {
            BankUserEntity user = (BankUserEntity) demoBankEntityManager.createNamedQuery(BankUserEntity.getByOlasId).setParameter(
                    "olasId", userId).getSingleResult();
            try {
                @SuppressWarnings("unchecked")
                List<BankAccountEntity> accounts = demoBankEntityManager.createNamedQuery(BankAccountEntity.getByUser).setParameter("user",
                        user).getResultList();
                for (BankAccountEntity account : accounts) {
                    demoBankEntityManager.remove(account);
                }
            } catch (NoResultException e) {
            }

            demoBankEntityManager.remove(user);
        } catch (NoResultException e) {
        }
    }

    private void removeDemoCinemaUser(String userId) {

        LOG.debug("remove demo cinema user id: " + userId);

        CinemaUserEntity user = demoCinemaEntityManager.find(CinemaUserEntity.class, userId);
        if (null != user) {
            Collection<CinemaTicketEntity> tickets = demoCinemaTicketService.getTickets(user);
            for (CinemaTicketEntity ticket : tickets) {
            	demoCinemaEntityManager.remove(ticket);
            }
            demoCinemaEntityManager.remove(user);
        }
    }

    private void removeDemoPaymentUser(String userId) {

        LOG.debug("remove demo payment user id: " + userId);

        PaymentUserEntity user = demoPaymentEntityManager.find(PaymentUserEntity.class, userId);
        if (null != user) {
            LOG.debug("removing demo payment user: " + user.getOlasName());
            List<PaymentEntity> payments = user.getPayments();
            for (PaymentEntity payment : payments) {
                demoPaymentEntityManager.remove(payment);
            }
            demoPaymentEntityManager.remove(user);
        }
    }
}
