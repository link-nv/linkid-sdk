/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.encap.webapp;

import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.webapp.AuthenticationPage.Goal;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;


/**
 * <h2>{@link EnablePage}<br>
 * <sub>Re-enable a device.</sub></h2>
 * 
 * <p>
 * Simple redirect to {@link AuthenticationPage} with {@link Goal#ENABLE_DEVICE}.
 * </p>
 * 
 * <p>
 * <i>Dec 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class EnablePage extends WebPage {

    public EnablePage() {

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        throw new RestartResponseException(new AuthenticationPage(Goal.ENABLE_DEVICE, protocolContext.getAttribute()));
    }
}
