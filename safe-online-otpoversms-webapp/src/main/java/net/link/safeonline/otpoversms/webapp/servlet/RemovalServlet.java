/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.otpoversms.webapp.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationManager;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The removal servlet implementation.
 * 
 * @author wvdhaute
 * 
 */
public class RemovalServlet extends AbstractInjectionServlet {

    private static final long       serialVersionUID = 1L;

    private static final Log        LOG              = LogFactory.getLog(RemovalServlet.class);

    @Init(name = "DeviceExitPath")
    private String                  deviceExitPath;

    @EJB(mappedName = OtpOverSmsDeviceService.JNDI_BINDING)
    private OtpOverSmsDeviceService otpOverSmsDeviceService;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger     securityAuditLogger;


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding(request, response);
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        handleLanding(request, response);
    }

    private void handleLanding(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String userId = DeviceOperationManager.getUserId(request.getSession());
        String mobile = DeviceOperationManager.getAttribute(request.getSession());
        LOG.debug("remove mobile " + mobile + " for user " + userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        protocolContext.setSuccess(false);

        try {
            this.otpOverSmsDeviceService.remove(userId, mobile);

            response.setStatus(HttpServletResponse.SC_OK);
            // notify that remove operation was successful.
            protocolContext.setSuccess(true);
        } catch (DeviceNotFoundException e) {
            LOG.error("device not found", e);
        } catch (SubjectNotFoundException e) {
            String message = "subject " + userId + " not found";
            LOG.error(message, e);
            this.securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, message);
        } catch (AttributeTypeNotFoundException e) {
            LOG.error("attribute type not found", e);
        } catch (AttributeNotFoundException e) {
            LOG.error("attribute not found", e);
        } catch (DeviceDisabledException e) {
            LOG.error("device disabled", e);
        }

        response.sendRedirect(this.deviceExitPath);

    }
}
