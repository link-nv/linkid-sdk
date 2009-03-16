/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.webapp;

import net.link.safeonline.wicket.web.OLASSession;

import org.apache.wicket.Request;


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


    public UserSession(Request request) {

        super(request);
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getUserId() {

        return userId;
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

}
