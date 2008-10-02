/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;


/**
 * <h2>{@link OlasAuthLink}<br>
 * <sub>A link that uses the OLAS SDK to log a user in through the OLAS authentication services.</sub></h2>
 * 
 * <p>
 * <i>Sep 22, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class OlasAuthLink extends Link<Object> {

    private static final long serialVersionUID = 1L;
    boolean                   login;


    /**
     * @param login
     *            <code>true</code>: Perform a <b>login</b> request.<br>
     *            <code>false</code>: Perform a <b>logout</b> request.
     */
    public OlasAuthLink(String id, boolean login) {

        super(id);

        this.login = login;
    }

    @Override
    public void onClick() {

        getRequestCycle().setRequestTarget(new IRequestTarget() {

            public void detach(RequestCycle requestCycle) {

            }

            public void respond(RequestCycle requestCycle) {

                HttpServletRequest request = ((WebRequest) requestCycle.getRequest()).getHttpServletRequest();
                HttpServletResponse response = ((WebResponse) requestCycle.getResponse()).getHttpServletResponse();
                String target = request.getServletPath();

                if (OlasAuthLink.this.login) {
                    SafeOnlineLoginUtils.login(target, request, response);
                } else {
                    SafeOnlineLoginUtils.logout(target, request, response);
                }
            }
        });
    }
}
