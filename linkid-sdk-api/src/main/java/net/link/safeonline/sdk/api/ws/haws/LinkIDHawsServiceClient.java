/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.haws;

import net.link.safeonline.sdk.api.haws.LinkIDPullException;
import net.link.safeonline.sdk.api.haws.LinkIDPushException;


/**
 * linkID HAWS authentication protocol WS client.
 * <p/>
 * Via this interface, service providers can push an authentication request to linkID and fetch the authentication response from it.
 */
public interface LinkIDHawsServiceClient<Request, Response> {

    String push(Request request, String language)
            throws LinkIDPushException;

    Response pull(String sessionId)
            throws LinkIDPullException;
}
