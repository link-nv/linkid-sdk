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
import org.apache.wicket.Application;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;


/**
 * <h2>{@link AbstractOlasAuthLink}<br>
 * <sub>A link that uses the OLAS SDK to log a user in through the OLAS authentication services.</sub></h2>
 * 
 * <p>
 * <i>Sep 22, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractOlasAuthLink extends Link<Object> implements OlasAuthDelegate {

    private static final long serialVersionUID = 1L;

    Log                       LOG              = LogFactory.getLog(getClass());
    Class<? extends Page>     requestTarget;
    boolean                   login;

    OlasAuthDelegate          delegate;


    public AbstractOlasAuthLink(String id) {

        this(id, null);
    }

    /**
     * @param target
     *            The {@link Page} to return to after the OLAS delegation. <code>null</code>: Use the application's homepage.
     */
    public AbstractOlasAuthLink(String id, Class<? extends Page> target) {

        super(id);

        if (target != null) {
            requestTarget = target;
        }

        delegate = this;
    }

    @Override
    public void onClick() {

        throw new RedirectResponseException(new IRequestTarget() {

            public void detach(RequestCycle requestCycle) {

            }

            public void respond(RequestCycle requestCycle) {

                HttpServletRequest request = ((WebRequest) requestCycle.getRequest()).getHttpServletRequest();
                HttpServletResponse response = ((WebResponse) requestCycle.getResponse()).getHttpServletResponse();
                Class<? extends Page> target = requestTarget == null? Application.get().getHomePage(): requestTarget;

                // The SDK does the rest.
                delegate.delegate(target, request, response);
            }
        });

    }
}
