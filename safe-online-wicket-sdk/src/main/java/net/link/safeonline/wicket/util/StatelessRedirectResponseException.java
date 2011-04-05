/*
 * SafeOnline project.
 *
 * Copyright (c) 2006-2011 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.wicket.util;

import org.apache.wicket.*;
import org.apache.wicket.protocol.http.WebResponse;


public class StatelessRedirectResponseException extends AbstractRestartResponseException {

    public StatelessRedirectResponseException(IRequestTarget target) {

        RequestCycle rc = RequestCycle.get();
        if (rc == null)
            throw new IllegalStateException( "This exception can only be thrown from within request processing cycle" );

        Response r = rc.getResponse();
        if (!(r instanceof WebResponse))
            throw new IllegalStateException( "This exception can only be thrown when wicket is processing an http request" );

        // abort any further response processing
        rc.setRequestTarget( target );
    }
}
