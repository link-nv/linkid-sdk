/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.demo.bank.entity.UserEntity;

import org.apache.wicket.Request;
import org.apache.wicket.Session;


/**
 * <h2>{@link BankSession}<br>
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
public class BankSession extends Session {

    private static final long serialVersionUID = 1L;

    private UserEntity        user;


    // USER ---------------------------------------------------------

    public void setUser(UserEntity user) {

        this.user = user;
    }

    public UserEntity getUser() {

        return this.user;
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if there is a user logged in and has a {@link UserEntity} set.
     */
    public static boolean isUserSet() {

        return get().getUser() != null;
    }

    // GLOBAL -------------------------------------------------------

    public BankSession(Request request) {

        super(request);
    }

    /**
     * @return The session for the current user.
     */
    public static BankSession get() {

        return (BankSession) Session.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupFeedbackMessages() {

    }
}
