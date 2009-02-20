/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.exception;

/**
 * Thrown in case a webservice was not found.
 * 
 * @author wvdhaute
 * 
 */
public class WSClientTransportException extends Exception {

    private static final long serialVersionUID = 1L;

    private String            location;


    public WSClientTransportException(String location) {

        super();
        this.location = location;
    }

    public String getLocation() {

        return location;
    }

    @Override
    public String getMessage() {

        return "Failed to contact webservice: " + location;
    }
}
