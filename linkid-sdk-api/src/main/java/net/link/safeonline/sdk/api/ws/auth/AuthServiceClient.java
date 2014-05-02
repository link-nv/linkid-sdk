/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.auth;

/**
 * linkID Auth WS client.
 * <p/>
 */
public interface AuthServiceClient<Request, Response> {

    /**
     * Start a linkID authentication.
     *
     * @param request   the authentication request
     * @param language  optional language (default is en)
     * @param userAgent optional user agent string, for adding e.g. callback params to the QR code URL, android chrome URL needs to be
     *                  http://linkidmauthurl/MAUTH/2/zUC8oA/eA==, ...
     */
    AuthnSession start(Request request, String language, String userAgent, boolean forceRegistration)
            throws AuthnException;

    /**
     * Poll the linkID authentication
     *
     * @param sessionId the sessionId of the authentication
     * @param language  optional language (default is en)
     *
     * @return poll response containing the state of the authentication.
     */
    PollResponse<Response> poll(String sessionId, String language)
            throws PollException;
}
