package net.link.safeonline.option.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationManager;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.option.webapp.OptionDevice;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The remove servlet implementation.
 * 
 * @author wvdhaute
 * 
 */
public class RemoveServlet extends AbstractInjectionServlet {

    private static final long   serialVersionUID = 1L;

    private static final Log    LOG              = LogFactory.getLog(RemoveServlet.class);

    @Init(name = "DeviceExitPath")
    private String              deviceExitPath;

    @EJB(mappedName = OptionDeviceService.JNDI_BINDING)
    private OptionDeviceService optionDeviceService;

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

        String imei = DeviceOperationManager.findAttribute(request.getSession());
        String userId = DeviceOperationManager.getUserId(request.getSession());
        LOG.debug("remove option device: " + imei + " for user " + userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        protocolContext.setSuccess(false);

        try {
            if (!OptionDevice.unregister(imei, request, response)) {
                LOG.error("Option device with IMEI " + imei + " not registered.");
            }

            else {
                optionDeviceService.remove(imei);

                response.setStatus(HttpServletResponse.SC_OK);
                // notify that disable operation was successful.
                protocolContext.setSuccess(true);
            }
        }

        catch (SubjectNotFoundException e) {
            String message = "subject " + userId + " not found";
            LOG.error(message, e);
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, message);
        } catch (DeviceRegistrationNotFoundException e) {
            LOG.error("Tried to remove a device that isn't registered.", e);
        }

        response.sendRedirect(deviceExitPath);
    }
}
