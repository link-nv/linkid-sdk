/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile;

import java.util.Map;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.auth.filter.LoginManager;


@SuppressWarnings("UnusedDeclaration")
public class AttributeBean {

    private HttpSession session;

    public AttributeBean() {

    }

    public HttpSession getSession() {

        return session;
    }

    public void setSession(HttpSession session) {

        this.session = session;
    }

    public Map getAttributes() {

        return LoginManager.findAttributes( session );
    }

    public String getUserId() {

        return LoginManager.findUserId( session );
    }
}
