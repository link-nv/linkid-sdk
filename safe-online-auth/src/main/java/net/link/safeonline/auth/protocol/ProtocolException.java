/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

/**
 * Thrown when a protocol handler detects a violation against the corresponding authentication protocol. The protocol error message will be
 * displayed in the protocol error message result page.
 * 
 * @author fcorneli
 * 
 */
public class ProtocolException extends Exception {

    private static final long serialVersionUID = 1L;

    private String            protocolName;


    /**
     * Main constructor.
     * 
     * @param message
     *            the protocol error message.
     */
    public ProtocolException(String message) {

        super(message);
        protocolName = "unknown";
    }

    public ProtocolException(String message, Throwable cause) {

        super(message, cause);
        protocolName = "unknown";
    }

    /**
     * Sets the protocol name.
     * 
     * @param protocolName
     */
    public void setProtocolName(String protocolName) {

        this.protocolName = protocolName;
    }

    /**
     * Gives back the protocol name.
     * 
     */
    public String getProtocolName() {

        return protocolName;
    }
}
