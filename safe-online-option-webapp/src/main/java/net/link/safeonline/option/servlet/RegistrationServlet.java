/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.option.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.model.option.exception.OptionAuthenticationException;
import net.link.safeonline.model.option.exception.OptionRegistrationException;
import net.link.safeonline.shared.SharedConstants;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link RegistrationServlet}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
public class RegistrationServlet extends AbstractInjectionServlet {

    private static final long    serialVersionUID = 1L;

    private static final Log     LOG              = LogFactory.getLog(RegistrationServlet.class);

    @EJB(mappedName = OptionDeviceService.JNDI_BINDING)
    private OptionDeviceService  optionDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    private SamlAuthorityService samlAuthorityService;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession();

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(session);
        try {

            protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());
            protocolContext.setSuccess(false);

            String imei = request.getParameter("imei");
            String pin = request.getParameter("pin");

            String userId = (String) session.getAttribute("userId");

            LOG.debug("registering imei: " + imei + " with pin: " + pin);
            optionDeviceService.register(userId, imei, pin);
            response.setStatus(HttpServletResponse.SC_OK);
            // notify that registration was successful.
            protocolContext.setSuccess(true);

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
        } catch (SubjectNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (DeviceNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (DeviceRegistrationNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        }
    }

}
