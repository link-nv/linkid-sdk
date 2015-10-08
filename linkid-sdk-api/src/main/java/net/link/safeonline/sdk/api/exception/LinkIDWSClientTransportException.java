/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.exception;

import javax.xml.ws.BindingProvider;


/**
 * Thrown in case a webservice was not found.
 *
 * @author wvdhaute
 */
public class LinkIDWSClientTransportException extends RuntimeException {

    private final transient BindingProvider bindingProvider;
    private                 String          location;

    public LinkIDWSClientTransportException(BindingProvider bindingProvider) {

        this.bindingProvider = bindingProvider;
        location = bindingProvider.getEndpointReference().toString();
    }

    public LinkIDWSClientTransportException(BindingProvider bindingProvider, Throwable cause) {

        super( cause );

        this.bindingProvider = bindingProvider;
        location = this.bindingProvider.getEndpointReference().toString();
    }

    public BindingProvider getBindingProvider() {

        return bindingProvider;
    }

    public String getLocation() {

        return location;
    }

    @Override
    public String getMessage() {

        return String.format( "[%s]: %s", bindingProvider.getEndpointReference().toString(), super.getMessage() );
    }
}
