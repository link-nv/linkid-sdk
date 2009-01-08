/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.payment.webapp;

import java.util.Locale;

import net.link.safeonline.demo.payment.entity.PaymentUserEntity;
import net.link.safeonline.wicket.web.OLASSession;

import org.apache.wicket.Request;
import org.apache.wicket.Session;


/**
 * <h2>{@link PaymentSession}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jun 10, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class PaymentSession extends OLASSession {

    private static final long  serialVersionUID = 1L;
    public static final Locale CURRENCY         = Locale.FRANCE;

    private PaymentUserEntity  user;
    private PaymentService     service;


    // USER ---------------------------------------------------------

    public void setUser(PaymentUserEntity user) {

        this.user = user;
    }

    public PaymentUserEntity getUser() {

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserOlasId() {

        return isUserSet()? getUser().getOlasId(): null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserSet() {

        return getUser() != null;
    }

    // SERVICE ------------------------------------------------------

    public void startService(PaymentService newService) {

        service = newService;
    }

    public void stopService() {

        service = null;
    }

    public PaymentService getService() {

        return service;
    }

    // GLOBAL -------------------------------------------------------

    public PaymentSession(Request request) {

        super(request);
    }

    /**
     * @return The session for the current user.
     */
    public static PaymentSession get() {

        return (PaymentSession) Session.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupFeedbackMessages() {

    }
}
