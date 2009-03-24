/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.saml2.response;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.sdk.auth.saml2.ResponseUtil;
import net.link.safeonline.sdk.ws.sts.TrustDomainType;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.StatusCode;


/**
 * Utility class for validating device operation responses.
 * 
 * @author wvdhaute
 * 
 */
public class DeviceOperationResponseUtil {

    private DeviceOperationResponseUtil() {

        // empty
    }

    /**
     * Validates a DeviceOperationResponse in the specified HTTP request. Checks:
     * <ul>
     * <li>response ID</li>
     * <li>response validated with STS WS location</li>
     * <li>at least 1 assertion present</li>
     * <li>assertion subject</li>
     * <li>assertion conditions notOnOrAfter and notBefore
     * </ul>
     * 
     * @param now
     * @param httpRequest
     * @param expectedInResponseTo
     * @param expectedDeviceOperation
     * @param stsWsLocation
     * @param certificate
     * @param privateKey
     * @throws ServletException
     */
    public static DeviceOperationResponse validateResponse(DateTime now, HttpServletRequest httpRequest, String expectedInResponseTo,
                                                           DeviceOperationType expectedDeviceOperation, String stsWsLocation,
                                                           X509Certificate certificate, PrivateKey privateKey, TrustDomainType trustDomain)
            throws ServletException {

        if (false == ResponseUtil.validateResponse(httpRequest, stsWsLocation, certificate, privateKey, trustDomain))
            return null;

        DeviceOperationResponse response = getDeviceOperationResponse(httpRequest);

        /*
         * Check whether the response is indeed a response to a previous request by comparing the InResponseTo fields
         */
        if (!response.getInResponseTo().equals(expectedInResponseTo))
            throw new ServletException("device operation response is not a response belonging to the original request.");

        if (!response.getDeviceOperation().equals(expectedDeviceOperation.name()))
            throw new ServletException(
                    "device operation response is not a response belonging to the original request, mismatch in device operation");

        if (response.getStatus().getStatusCode().getValue().equals(StatusCode.REQUEST_UNSUPPORTED_URI)
                || response.getStatus().getStatusCode().getValue().equals(DeviceOperationResponse.FAILED_URI))
            /**
             * Device Operation failed but response ok.
             */
            return response;

        if (response.getDeviceOperation().equals(DeviceOperationType.NEW_ACCOUNT_REGISTER.name())) {

            List<Assertion> assertions = response.getAssertions();
            if (assertions.isEmpty())
                throw new ServletException("missing Assertion");

            for (Assertion assertion : assertions) {
                ResponseUtil.validateAssertion(assertion, now, null);
            }
        }
        return response;
    }

    /**
     * Returns the {@link DeviceOperationResponse} embedded in the request. Throws a {@link ServletException} if not found or of the wrong
     * type.
     * 
     * @param request
     * @throws ServletException
     */
    public static DeviceOperationResponse getDeviceOperationResponse(HttpServletRequest request)
            throws ServletException {

        SAMLObject samlObject = ResponseUtil.getSAMLObject(request);
        if (false == samlObject instanceof DeviceOperationResponse)
            throw new ServletException("SAML message not a device operation response message");
        return (DeviceOperationResponse) samlObject;
    }

}
