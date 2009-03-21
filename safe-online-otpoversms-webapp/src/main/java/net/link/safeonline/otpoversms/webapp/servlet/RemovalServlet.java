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
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.manage.saml2.DeviceOperationManager;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.node.util.AbstractNodeInjectionServlet;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The removal servlet implementation.
 * 
 * @author wvdhaute
 * 
 */
public class RemovalServlet extends AbstractNodeInjectionServlet {

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
        String attributeId = DeviceOperationManager.getAttributeId(request.getSession());
        LOG.debug("remove mobile " + attributeId + " for user " + userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        protocolContext.setSuccess(false);

        try {
            otpOverSmsDeviceService.remove(userId, attributeId);

            response.setStatus(HttpServletResponse.SC_OK);
            // notify that remove operation was successful.
            protocolContext.setSuccess(true);
        } catch (SubjectNotFoundException e) {
            String message = "subject " + userId + " not found";
            LOG.error(message, e);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, message);
        } catch (DeviceRegistrationNotFoundException e) {
            LOG.error("Tried to remove a device that wasn't registered.", e);
        } catch (AttributeNotFoundException e) {
            LOG.error("Attribute not found", e);
        } catch (AttributeTypeNotFoundException e) {
            LOG.error("Attribute type not found", e);
        }

        response.sendRedirect(deviceExitPath);

    }
}
