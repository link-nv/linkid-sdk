/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Page;


/**
 * <h2>{@link OlasAuthDelegate}<br>
 * <sub>Simple interface to provide delegation of olas authentication requests.</sub></h2>
 * 
 * <p>
 * <i>Apr 3, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public interface OlasAuthDelegate extends Serializable {

    /**
     * Override this method to implement or delegate the actual OLAS operation.
     * 
     * @param target
     *            The wicket page to return to after OLAS delegation.
     */
    void delegate(Class<? extends Page> target, HttpServletRequest request, HttpServletResponse response);
}
