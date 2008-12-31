/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.bank.webapp;

import java.util.Locale;

import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.wicket.web.OLASSession;

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
public class BankSession extends OLASSession {

    private static final long  serialVersionUID = 1L;
    public static final Locale CURRENCY         = Locale.FRANCE;

    private BankUserEntity     user;

    private String             linkingBankId;


    // USER ---------------------------------------------------------

    public void setUser(BankUserEntity user) {

        this.user = user;
    }

    public BankUserEntity getUser() {

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

    /**
     * @return The bankId of the user that is being linked to an OLAS account.
     */
    public String getLinkingUser() {

        return linkingBankId;
    }

    /**
     * Notify the session that the given user's bank account is being linked to an OLAS account.
     */
    public void setLinkingUser(BankUserEntity user) {

        linkingBankId = user == null? null: user.getBankId();
    }

    /**
     * Operates on the current session.
     * 
     * @return <code>true</code> if the currently logged in user has requested his account be linked to an OLAS account.
     */
    public static boolean isLinking() {

        return get().isUserSet() && get().getUser().getBankId().equals(get().getLinkingUser());
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
