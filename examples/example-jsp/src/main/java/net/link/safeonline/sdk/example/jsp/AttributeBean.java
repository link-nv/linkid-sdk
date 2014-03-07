/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.jsp;

import java.util.Map;
import javax.servlet.http.HttpSession;
import net.link.safeonline.sdk.api.ws.attrib.client.AttributeClient;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;


public class AttributeBean {

    private HttpSession session;

    private AttributeClient attributeClient;

    public AttributeBean() {
        System.err.println("BEAN CREATED");
        attributeClient = LinkIDServiceFactory.getAttributeService();
    }

    public HttpSession getSession() {
        return session;
    }

    public void setSession(HttpSession session) {
        System.err.println("SESSION SET");
        this.session = session;
    }

    public Map getTestAttributes() {
        System.err.println("GET ATTRIBUTES");
        try {
            return attributeClient.getAttributes( LoginManager.findUserId( session ));
        }
        catch (Exception e) {
            return null;
        }
    }

    public void setTestAttributes(Map value) {
        // empty
    }
}
