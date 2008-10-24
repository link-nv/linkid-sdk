/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import net.link.safeonline.demo.payment.PaymentConstants;
import net.link.safeonline.demo.payment.PaymentServiceEntry;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;


@Stateful
@Name("paymentServiceEntry")
@LocalBinding(jndiBinding = "SafeOnlinePaymentDemo/PaymentServiceEntryBean/local")
public class PaymentServiceEntryBean implements PaymentServiceEntry {

    @Logger
    private Log     log;

    @RequestParameter("target")
    @Out(value = "target", scope = ScopeType.SESSION, required = false)
    private String  target;

    @RequestParameter("user")
    @Out(value = "user", scope = ScopeType.SESSION, required = false)
    private String  user;

    @RequestParameter("recipient")
    @Out(value = "recipient", scope = ScopeType.SESSION, required = false)
    private String  recipient;

    @RequestParameter("amount")
    @Out(value = "amount", scope = ScopeType.SESSION, required = false)
    private Double  amount;

    @SuppressWarnings("unused")
    @RequestParameter("message")
    @Out(value = "message", scope = ScopeType.SESSION, required = false)
    private String  message;

    @In(create = true)
    FacesMessages   facesMessages;

    @In
    private Context sessionContext;


    @Remove
    @Destroy
    public void destroyCallback() {

    }

    public void init() {

        this.log.debug("init; username #0; recipient #1; amount #2", this.user, this.recipient, this.amount);
        if (null == this.user) {
            this.facesMessages.add("user request parameter is null");
        }
        if (null == this.target) {
            this.facesMessages.add("target request parameter is null");
        }
        if (null == this.recipient) {
            this.facesMessages.add("recipient request parameter is null");
        }
        if (null == this.amount) {
            this.facesMessages.add("amount request parameter is null");
        }

        this.sessionContext.set("role", PaymentConstants.AUTHENTICATED_ROLE);

    }
}
