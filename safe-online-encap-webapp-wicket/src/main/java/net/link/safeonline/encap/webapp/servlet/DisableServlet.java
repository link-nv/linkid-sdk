package net.link.safeonline.encap.webapp.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationManager;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The disable servlet implementation.
 * 
 * @author wvdhaute
 * 
 */
public class DisableServlet extends AbstractInjectionServlet {

    private static final long   serialVersionUID = 1L;

    private static final Log    LOG              = LogFactory.getLog(DisableServlet.class);

    @Init(name = "DeviceExitPath")
    private String              deviceExitPath;

    @EJB(mappedName = EncapDeviceService.JNDI_BINDING)
    private EncapDeviceService  encapDeviceService;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger securityAuditLogger;


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

        String attribute = DeviceOperationManager.getAttribute(request.getSession());
        String userId = DeviceOperationManager.getUserId(request.getSession());
        LOG.debug("disable encap device: " + attribute + " for user " + userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        protocolContext.setSuccess(false);

        try {
            encapDeviceService.disable(userId, attribute);

            response.setStatus(HttpServletResponse.SC_OK);
            // notify that disable operation was successful.
            protocolContext.setSuccess(true);
        } catch (MobileException e) {
            LOG.error("mobile service caused:", e);
        } catch (DeviceNotFoundException e) {
            LOG.error("device not found", e);
        } catch (SubjectNotFoundException e) {
            String message = "subject " + userId + " not found";
            LOG.error(message, e);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, message);
        } catch (DeviceRegistrationNotFoundException e) {
            String message = "device registration \"" + attribute + "\" not found";
            LOG.error(message, e);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, message);
        }

        response.sendRedirect(deviceExitPath);

    }
}
