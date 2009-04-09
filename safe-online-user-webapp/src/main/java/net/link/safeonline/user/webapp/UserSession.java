/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.webapp;

import net.link.safeonline.wicket.web.OLASSession;

import org.apache.wicket.Request;
import org.apache.wicket.Session;


/**
 * <h2>{@link UserSession}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 16, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class UserSession extends OLASSession {

    private static final long serialVersionUID = 1L;

    private String            userId           = null;

    private String            session          = null;


    public UserSession(Request request) {

        super(request);
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getUserId() {

        return userId;
    }

    public void setSession(String session) {

        this.session = session;
    }

    public String getSession() {

        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserOlasId() {

        return userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserSet() {

        return getUserId() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean logout() {

        userId = null;
        return true;
    }

    /**
     * @return The session for the current user.
     */
    public static UserSession get() {

        return (UserSession) Session.get();
    }

}
