/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.sts;

import net.link.safeonline.sdk.api.exception.LinkIDValidationFailedException;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;


/**
 * Interface for Security Token Service WS-Trust client.
 *
 * @author fcorneli
 */
public interface LinkIDSecurityTokenServiceClient<R> {

    /**
     * Validate the SAML v2.0 Authentication Response.
     *
     * @param response           the SAML v2.0 Authentication Response.
     * @param requestIssuer      the SAML v2.0 Authentication Request Issuer for this response. Will be used for validation of audience
     *                           restrictions in the response.
     * @param requestQueryString HTTP Servlet Request query string
     * @param requestURL         HTTP Servlet Request URL
     *
     * @throws LinkIDWSClientTransportException something went wrong sending the STS Validation Request.
     * @throws LinkIDValidationFailedException  validation failed of the SAML v2.0 Authentication Response.
     */
    void validateResponse(R response, String requestIssuer, String requestQueryString, StringBuffer requestURL)
            throws LinkIDWSClientTransportException, LinkIDValidationFailedException;
}
