/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.option.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.model.option.OptionConstants;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.model.option.exception.OptionAuthenticationException;
import net.link.safeonline.model.option.exception.OptionRegistrationException;
import net.link.safeonline.shared.SharedConstants;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Authentication Servlet that accepts an imei and pin code.
 * 
 * @author dhouthoo
 * 
 */
public class AuthenticationServlet extends AbstractInjectionServlet {

    private static final long    serialVersionUID = 1L;

    private static final Log     LOG              = LogFactory.getLog(AuthenticationServlet.class);

    @EJB(mappedName = "SafeOnlineOption/OptionDeviceServiceBean/local")
    private OptionDeviceService  optionDeviceService;

    @EJB(mappedName = "SafeOnline/SamlAuthorityServiceBean/local")
    private SamlAuthorityService samlAuthorityService;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response) throws IOException,
            ServletException {

        try {
            AuthenticationContext authenticationContext = AuthenticationContext.getAuthenticationContext(request
                    .getSession());
            authenticationContext.setUsedDevice(OptionConstants.OPTION_DEVICE_ID);

            String imei = request.getParameter("imei");
            String pin = request.getParameter("pin");

            LOG.debug("authenticating imei: " + imei + " with pin: " + pin);
            String deviceUserId = this.optionDeviceService.authenticate(imei, pin);

            authenticationContext.setUserId(deviceUserId);
            authenticationContext.setValidity(this.samlAuthorityService.getAuthnAssertionValidity());

        } catch (SubjectNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (OptionAuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (OptionRegistrationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (AttributeTypeNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (AttributeNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (DeviceDisabledException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        }
    }
}
