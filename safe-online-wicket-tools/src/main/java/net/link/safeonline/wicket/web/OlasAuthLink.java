/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.wicket.tools.RedirectResponseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
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
public abstract class OlasAuthLink extends Link<Object> {

    private static final long serialVersionUID = 1L;

    Log                       LOG              = LogFactory.getLog(getClass());
    String                    requestTarget;
    boolean                   login;


    public OlasAuthLink(String id) {

        this(id, null);
    }

    public OlasAuthLink(String id, Class<? extends Page> target) {

        super(id);

        if (target != null) {
            this.requestTarget = RequestCycle.get().urlFor(target, null).toString();
        }
    }

    @Override
    public void onClick() {

        throw new RedirectResponseException(new IRequestTarget() {

            public void detach(RequestCycle requestCycle) {

            }

            public void respond(RequestCycle requestCycle) {

                HttpServletRequest request = ((WebRequest) requestCycle.getRequest()).getHttpServletRequest();
                HttpServletResponse response = ((WebResponse) requestCycle.getResponse()).getHttpServletResponse();

                // Where do we go to after the whole operation?
                String realTarget = OlasAuthLink.this.requestTarget;
                if (realTarget == null) {
                    realTarget = request.getServletPath();
                }

                // The SDK does the rest.
                delegate(realTarget, request, response);
            }
        });

    }

    /**
     * Override this method to implement or delegate the actual OLAS operation.
     * 
     * @param target
     *            The URL-encoded location that we request to end up after this whole operation.
     */
    protected abstract void delegate(String target, HttpServletRequest request, HttpServletResponse response);
}
