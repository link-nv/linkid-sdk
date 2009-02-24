/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sms.clickatell;

import net.link.safeonline.sms.clickatell.exception.ClickatellException;

/**
 * <h2>{@link ClickatellChannel}<br>
 * <sub>Abstracts the different channels that are supported by Clickatell</sub></h2>
 * 
 * <p>
 * Abstracts the different channels that are supported by Clickatell such as SOAP, HTTP, SMPP, ...
 * </p>
 * 
 * <p>
 * <i>Feb 20, 2009</i>
 * </p>
 * 
 * @author dhouthoo
 */
public interface ClickatellChannel {

    public void send(String mobile, String message)
            throws ClickatellException;

}
