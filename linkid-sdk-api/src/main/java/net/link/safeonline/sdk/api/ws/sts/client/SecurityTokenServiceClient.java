/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.sts.client;

import net.link.safeonline.sdk.api.exception.ValidationFailedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;


/**
 * Interface for Security Token Service WS-Trust client.
 *
 * @author fcorneli
 */
public interface SecurityTokenServiceClient<R, LR, LQ> {

    /**
     * Validate the SAML v2.0 Authentication Response.
     *
     * @param response           the SAML v2.0 Authentication Response.
     * @param requestIssuer      the SAML v2.0 Authentication Request Issuer for this response. Will be used for validation of audience
     *                           restrictions in the response.
     * @param requestQueryString HTTP Servlet Request query string
     * @param requestURL         HTTP Servlet Request URL
     *
     * @throws WSClientTransportException something went wrong sending the STS Validation Request.
     * @throws ValidationFailedException  validation failed of the SAML v2.0 Authentication Response.
     */
    void validateResponse(R response, String requestIssuer, String requestQueryString, StringBuffer requestURL)
            throws WSClientTransportException, ValidationFailedException;

    /**
     * Validate the SAML v2.0 Logout Response
     *
     * @param logoutResponse     the SAML v2.0 Logout Response.
     * @param requestQueryString HTTP Servlet Request query string
     * @param requestURL         HTTP Servlet Request URL
     *
     * @throws WSClientTransportException something went wrong sending the STS Validation Request.
     * @throws ValidationFailedException  validation failed of the SAML v2.0 Logout Response.
     */
    void validateLogoutResponse(LR logoutResponse, String requestQueryString, StringBuffer requestURL)
            throws WSClientTransportException, ValidationFailedException;

    /**
     * Validate the SAML v2.0 Logout Request.
     *
     * @param logoutRequest      the SAML v2.0 Logout Request.
     * @param requestQueryString HTTP Servlet Request query string
     * @param requestURL         HTTP Servlet Request URL
     *
     * @throws WSClientTransportException something went wrong sending the STS Validation Request.
     * @throws ValidationFailedException  validation failed of the SAML v2.0 Logout Request.
     */
    void validateLogoutRequest(LQ logoutRequest, String requestQueryString, StringBuffer requestURL)
            throws WSClientTransportException, ValidationFailedException;
}
