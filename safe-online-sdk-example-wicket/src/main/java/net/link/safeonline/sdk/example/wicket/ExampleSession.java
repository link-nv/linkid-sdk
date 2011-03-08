/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.example.wicket;

import net.link.safeonline.wicket.LinkIDSession;
import org.apache.wicket.Request;
import org.apache.wicket.Session;


/**
 * <h2>{@link ExampleSession}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Oct 22, 2008</i> </p>
 *
 * @author wvdhaute
 */
public class ExampleSession extends LinkIDSession {

    private String userId = null;

    public ExampleSession(Request request) {

        super( request );
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String findUserLinkID() {

        return userId;
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
     * {@inheritDoc}
     */
    @Override
    public boolean isUserSet() {

        return userId != null;
    }

    /**
     * @return The session for the current user.
     */
    public static ExampleSession get() {

        return (ExampleSession) Session.get();
    }
}
