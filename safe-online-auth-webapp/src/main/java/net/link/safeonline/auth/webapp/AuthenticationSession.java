/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.webapp;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;


/**
 * <h2>{@link AuthenticationSession}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 4, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AuthenticationSession extends WebSession {

    private static final long serialVersionUID = 1L;

    private String            userId           = null;

    private String            loginName        = null;


    public AuthenticationSession(Request request) {

        super(request);
    }

    public static AuthenticationSession get() {

        return (AuthenticationSession) Session.get();
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public String getUserId() {

        return userId;
    }

    public void setLoginName(String loginName) {

        this.loginName = loginName;

    }

    public String getLoginName() {

        return loginName;
    }
}
