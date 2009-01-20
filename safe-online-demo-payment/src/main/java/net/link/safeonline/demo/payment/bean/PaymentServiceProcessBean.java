/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.payment.PaymentServiceProcess;
import net.link.safeonline.demo.payment.entity.PaymentEntity;
import net.link.safeonline.demo.payment.entity.UserEntity;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;
import org.jboss.seam.web.Session;


@Stateful
@Name("paymentServiceProcess")
@LocalBinding(jndiBinding = PaymentServiceProcess.JNDI_BINDING)
public class PaymentServiceProcessBean extends AbstractPaymentDataClientBean implements PaymentServiceProcess {

    @Logger
    private Log            log;

    @Resource
    private SessionContext sessionContext;

    @PersistenceContext(unitName = "DemoPaymentEntityManager")
    private EntityManager  entityManager;

    @In("user")
    private String         user;

    @In("recipient")
    private String         recipient;

    @In("amount")
    private Double         amount;

    @In(value = "message", required = false)
    private String         message;

    @In("target")
    private String         target;

    private String         visa;

    @SuppressWarnings("unused")
    @Out(value = "visaNumber", required = false)
    private String         visaNumber;


    public String getUsername() {

        String username = getUsername(getUserId());
        log.debug("username #0", username);
        return username;
    }

    private String getUserId() {

        Principal principal = sessionContext.getCallerPrincipal();
        return principal.getName();
    }

    public String authenticate() {

        log.debug("authenticate");
        String result = SafeOnlineLoginUtils.login("cards.seam");
        return result;
    }

    public String commit() {

        log.debug("commit");
        String username = getUsername();
        if (null == username) {
            facesMessages.add("username is null. user not authenticated");
            return null;
        }
        if (false == user.equals(username)) {
            facesMessages.add("authenticated user != requested user");
            return null;
        }
        UserEntity targetUser = entityManager.find(UserEntity.class, getUserId());
        if (targetUser == null) {
            targetUser = new UserEntity(getUserId(), username);
            entityManager.persist(targetUser);
        }

        Date paymentDate = new Date();
        PaymentEntity newPayment = new PaymentEntity();
        newPayment.setRecipient(recipient);
        newPayment.setAmount(amount);
        newPayment.setMessage(message);
        newPayment.setPaymentDate(paymentDate);
        newPayment.setVisa(visa);
        newPayment.setOwner(targetUser);

        entityManager.persist(newPayment);

        visaNumber = visa;

        return "success";
    }

    public String done() {

        log.debug("done. redirect to #0", target);
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        Session.getInstance().invalidate();
        try {
            externalContext.redirect(target);
        } catch (IOException e) {
            facesMessages.add("redirect error");
        }
        return null;
    }

    public String getVisa() {

        return visa;
    }

    public void setVisa(String visa) {

        this.visa = visa;
    }
}
