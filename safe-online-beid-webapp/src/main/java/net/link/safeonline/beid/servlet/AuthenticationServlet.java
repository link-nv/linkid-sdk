/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.DecodingException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PkiExpiredException;
import net.link.safeonline.authentication.exception.PkiInvalidException;
import net.link.safeonline.authentication.exception.PkiNotYetValidException;
import net.link.safeonline.authentication.exception.PkiRevokedException;
import net.link.safeonline.authentication.exception.PkiSuspendedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AuthenticationStatement;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.model.beid.BeIdDeviceService;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.servlet.AbstractStatementServlet;
import net.link.safeonline.shared.SharedConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Authentication Servlet that accepts authentication statements from the client-side browser applet.
 * 
 * @author fcorneli
 * 
 */
public class AuthenticationServlet extends AbstractStatementServlet {

    private static final long    serialVersionUID = 1L;

    private static final Log     LOG              = LogFactory.getLog(AuthenticationServlet.class);

    @EJB(mappedName = BeIdDeviceService.JNDI_BINDING)
    private BeIdDeviceService    beIdDeviceService;

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    private SamlAuthorityService samlAuthorityService;


    @Override
    protected void processStatement(byte[] statementData, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {

        String sessionId = session.getId();
        LOG.debug("session Id: " + sessionId);

        try {
            AuthenticationContext authenticationContext = AuthenticationContext.getAuthenticationContext(session);
            authenticationContext.setUsedDevice(BeIdConstants.BEID_DEVICE_ID);

            AuthenticationStatement authenticationStatement = new AuthenticationStatement(statementData);
            String deviceUserId = beIdDeviceService.authenticate(sessionId, authenticationContext.getApplication(),
                    authenticationStatement);

            authenticationContext.setUserId(deviceUserId);
            authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

        } catch (DecodingException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (TrustDomainNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (SubjectNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (ArgumentIntegrityException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (PkiRevokedException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (PkiSuspendedException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (PkiExpiredException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (PkiNotYetValidException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (PkiInvalidException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (DeviceNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        } catch (DeviceDisabledException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(SharedConstants.SAFE_ONLINE_ERROR_HTTP_HEADER, e.getErrorCode());
        }
    }
}
