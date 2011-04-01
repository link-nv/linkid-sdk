/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.sts;

import javax.servlet.http.HttpServletRequest;
import net.link.safeonline.sdk.logging.exception.ValidationFailedException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.Response;


/**
 * Interface for Security Token Service WS-Trust client.
 *
 * @author fcorneli
 */
public interface SecurityTokenServiceClient {

    /**
     * Validate the SAML v2.0 Authentication Response.
     *
     * @param response      the SAML v2.0 Authentication Response.
     * @param requestIssuer the SAML v2.0 Authentication Request Issuer for this response. Will be used for validation of audience
     *                      restrictions in the response.
     * @param request       HTTP Servlet Request
     *
     * @throws WSClientTransportException something went wrong sending the STS Validation Request.
     * @throws ValidationFailedException  validation failed of the SAML v2.0 Authentication Response.
     */
    void validate(Response response, String requestIssuer, HttpServletRequest request)
            throws WSClientTransportException, ValidationFailedException;

    /**
     * Validate the SAML v2.0 Logout Response
     *
     * @param logoutResponse the SAML v2.0 Logout Response.
     * @param request        HTTP Servlet Request
     *
     * @throws WSClientTransportException something went wrong sending the STS Validation Request.
     * @throws ValidationFailedException  validation failed of the SAML v2.0 Logout Response.
     */
    void validate(LogoutResponse logoutResponse, HttpServletRequest request)
            throws WSClientTransportException, ValidationFailedException;

    /**
     * Validate the SAML v2.0 Logout Request.
     *
     * @param logoutRequest the SAML v2.0 Logout Request.
     * @param request       HTTP Servlet Request
     *
     * @throws WSClientTransportException something went wrong sending the STS Validation Request.
     * @throws ValidationFailedException  validation failed of the SAML v2.0 Logout Request.
     */
    void validate(LogoutRequest logoutRequest, HttpServletRequest request)
            throws WSClientTransportException, ValidationFailedException;
}
