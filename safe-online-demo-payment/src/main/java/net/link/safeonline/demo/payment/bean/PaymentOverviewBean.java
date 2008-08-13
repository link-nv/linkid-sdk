/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.payment.PaymentOverview;
import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.UserEntity;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.log.Log;


@Stateful
@Name("paymentOverview")
@LocalBinding(jndiBinding = "SafeOnlinePaymentDemo/PaymentOverviewBean/local")
@SecurityDomain("demo-payment")
public class PaymentOverviewBean extends AbstractPaymentDataClientBean implements PaymentOverview {

    @Logger
    private Log                 log;

    @Resource
    private SessionContext      sessionContext;

    @PersistenceContext(unitName = "DemoPaymentEntityManager")
    private EntityManager       entityManager;

    public static final String  PAYMENT_LIST_NAME = "paymentList";

    @DataModel(PAYMENT_LIST_NAME)
    @SuppressWarnings("unused")
    private List<PaymentEntity> paymentList;


    @Factory(PAYMENT_LIST_NAME)
    @RolesAllowed("user")
    public void paymentListFactory() {

        UserEntity user = this.entityManager.find(UserEntity.class, getUsername());
        if (user == null) {
            user = new UserEntity(this.getUsername());
            this.entityManager.persist(user);
        }
        this.paymentList = user.getPayments();
    }

    private String getUsername() {

        Principal principal = this.sessionContext.getCallerPrincipal();
        String userId = principal.getName();
        String username = getUsername(userId);
        this.log.debug("username #0", username);
        return username;
    }
}
