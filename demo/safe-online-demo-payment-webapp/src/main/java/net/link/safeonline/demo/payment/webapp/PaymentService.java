/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.webapp;

import java.io.Serializable;


/**
 * <h2>{@link PaymentService}<br>
 * <sub>A Payment Service Request.</sub></h2>
 * 
 * <p>
 * This object represents a service request from an application to the payment application to perform a payment by the user to a certain
 * recipient.
 * </p>
 * 
 * <p>
 * <i>Dec 30, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class PaymentService implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            recipient;
    private Double            amount;
    private String            message;
    private String            target;


    /**
     * Create a new payment service request.
     */
    public PaymentService(String recipient, Double amount, String message, String target) {

        this.recipient = recipient;
        this.amount = amount;
        this.message = message;
        this.target = target;
    }

    public String getRecipient() {

        return recipient;
    }

    public Double getAmount() {

        return amount;
    }

    public String getMessage() {

        return message;
    }

    public String getTarget() {

        return target;
    }
}
