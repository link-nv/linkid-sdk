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

    /**
     * This methods sends the given regular text as an sms to the specified number.
     * 
     * @param mobile
     *            The mobile number to which an sms needs to be sent
     * @param message
     *            The regular text content of the message
     */
    public void send(String mobile, String message)
            throws ClickatellException;

}
