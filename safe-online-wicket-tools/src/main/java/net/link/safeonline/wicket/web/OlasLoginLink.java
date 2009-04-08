/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.seam.SafeOnlineAuthenticationUtils;

import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;


/**
 * <h2>{@link OlasLoginLink}<br>
 * <sub>A link that uses the OLAS SDK to log a user in through the OLAS authentication services.</sub></h2>
 * 
 * <p>
 * <i>Sep 22, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class OlasLoginLink extends AbstractOlasAuthLink {

    private static final long serialVersionUID    = 1L;
    private Integer           color;
    private Boolean           minimal;
    private Boolean           forceAuthentication = false;


    public OlasLoginLink(String id) {

        super(id);
    }

    public OlasLoginLink(String id, Class<? extends Page> target) {

        super(id, target);
    }

    public OlasLoginLink(String id, Class<? extends Page> target, Integer color, Boolean minimal, Boolean forceAuthentication,
                         String session) {

        super(id, target);

        this.color = color;
        this.minimal = minimal;
        this.forceAuthentication = forceAuthentication;
        this.session = session;
    }

    /**
     * @param color
     *            The color of olas-auth theme.
     */
    public void setColor(Integer color) {

        this.color = color;
    }

    /**
     * @param minimal
     *            <code>true</code>: Hide header & footer in olas-auth.
     */
    public void setMinimal(Boolean minimal) {

        this.minimal = minimal;
    }

    /**
     * @param forceAuthentication
     */
    public void setForceAuthentication(Boolean forceAuthentication) {

        this.forceAuthentication = forceAuthentication;
    }

    /**
     * {@inheritDoc}
     */
    public void delegate(Class<? extends Page> target, HttpServletRequest request, HttpServletResponse response) {

        String targetUrl = RequestCycle.get().urlFor(target, null).toString();
        Locale locale = Session.exists()? Session.get().getLocale(): request.getLocale();

        SafeOnlineAuthenticationUtils.login(targetUrl, locale, color, minimal, forceAuthentication, session, request, response);
    }
}
