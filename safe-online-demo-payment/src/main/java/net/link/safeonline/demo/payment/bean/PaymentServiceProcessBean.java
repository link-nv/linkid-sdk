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
@LocalBinding(jndiBinding = "SafeOnlinePaymentDemo/PaymentServiceProcessBean/local")
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
        this.log.debug("username #0", username);
        return username;
    }

    private String getUserId() {

        Principal principal = this.sessionContext.getCallerPrincipal();
        return principal.getName();
    }

    public String authenticate() {

        this.log.debug("authenticate");
        String result = SafeOnlineLoginUtils.login("cards.seam");
        return result;
    }

    public String commit() {

        this.log.debug("commit");
        String username = getUsername();
        if (null == username) {
            this.facesMessages.add("username is null. user not authenticated");
            return null;
        }
        if (false == this.user.equals(username)) {
            this.facesMessages.add("authenticated user != requested user");
            return null;
        }
        UserEntity targetUser = this.entityManager.find(UserEntity.class, username);
        if (targetUser == null) {
            targetUser = new UserEntity(username);
            this.entityManager.persist(targetUser);
        }

        Date paymentDate = new Date();
        PaymentEntity newPayment = new PaymentEntity();
        newPayment.setRecipient(this.recipient);
        newPayment.setAmount(this.amount);
        newPayment.setMessage(this.message);
        newPayment.setPaymentDate(paymentDate);
        newPayment.setVisa(this.visa);
        newPayment.setOwner(targetUser);

        this.entityManager.persist(newPayment);

        this.visaNumber = this.visa;

        return "success";
    }

    public String done() {

        this.log.debug("done. redirect to #0", this.target);
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        Session.getInstance().invalidate();
        try {
            externalContext.redirect(this.target);
        } catch (IOException e) {
            this.facesMessages.add("redirect error");
        }
        return null;
    }

    public String getVisa() {

        return this.visa;
    }

    public void setVisa(String visa) {

        this.visa = visa;
    }
}
