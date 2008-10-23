/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.webapp;

import java.util.Locale;

import net.link.safeonline.demo.bank.entity.BankUserEntity;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;


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
public class BankSession extends WebSession {

    private static final long  serialVersionUID = 1L;
    public static final Locale CURRENCY         = Locale.FRANCE;

    private BankUserEntity     user;

    private String             linkingBankId;


    // USER ---------------------------------------------------------

    public void setUser(BankUserEntity user) {

        this.user = user;
    }

    public BankUserEntity getUser() {

        return this.user;
    }

    /**
     * @return The bankId of the user that is being linked to an OLAS account.
     */
    public String getLinkingUser() {

        return this.linkingBankId;
    }

    /**
     * Notify the session that the given user's bank account is being linked to an OLAS account.
     */
    public void setLinkingUser(BankUserEntity user) {

        this.linkingBankId = user == null? null: user.getBankId();
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if there is a user logged in and has a {@link BankUserEntity} set.
     */
    public static boolean isUserSet() {

        return get().getUser() != null;
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the currently logged in user has requested his account be linked to an OLAS account.
     */
    public static boolean isLinking() {

        return isUserSet() && get().getUser().getBankId().equals(get().getLinkingUser());
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
