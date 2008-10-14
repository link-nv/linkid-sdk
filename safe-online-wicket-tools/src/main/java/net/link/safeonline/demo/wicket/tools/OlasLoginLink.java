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

import org.apache.wicket.Page;


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
public class OlasLoginLink extends OlasAuthLink {

    private static final long serialVersionUID = 1L;


    public OlasLoginLink(String id) {

        super(id);
    }

    public OlasLoginLink(String id, Class<? extends Page> target) {

        super(id, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void delegate(String target, HttpServletRequest request, HttpServletResponse response) {

        SafeOnlineLoginUtils.login(target, request, response);
    }
}
