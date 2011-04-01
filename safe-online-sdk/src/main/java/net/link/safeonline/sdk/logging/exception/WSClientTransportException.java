/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.logging.exception;

import javax.xml.ws.BindingProvider;


/**
 * Thrown in case a webservice was not found.
 *
 * @author wvdhaute
 */
public class WSClientTransportException extends Exception {

    private final transient BindingProvider bindingProvider;
    private                 String          location;

    public WSClientTransportException(BindingProvider bindingProvider) {

        this.bindingProvider = bindingProvider;
        location = bindingProvider.getEndpointReference().toString();
    }

    public WSClientTransportException(BindingProvider bindingProvider, Throwable cause) {

        super( cause );

        this.bindingProvider = bindingProvider;
        location = this.bindingProvider.getEndpointReference().toString();
    }

    public BindingProvider getBindingProvider() {

        return bindingProvider;
    }

    @Override
    public String getMessage() {

        return String.format( "[%s]: %s", bindingProvider.getEndpointReference().toString(), super.getMessage() );
    }
}
