package net.link.safeonline.otpoversms.webapp.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
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

    private static final long       serialVersionUID = 1L;

    private static final Log        LOG              = LogFactory.getLog(DisableServlet.class);

    @Init(name = "DeviceExitPath")
    private String                  deviceExitPath;

    @EJB(mappedName = OtpOverSmsDeviceService.JNDI_BINDING)
    private OtpOverSmsDeviceService otpOverSmsDeviceService;


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

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        protocolContext.setSuccess(false);

        String attribute = DeviceOperationManager.findAttribute(request.getSession());
        if (null == attribute) {
            LOG.debug("attribute not found");
            response.sendRedirect(this.deviceExitPath);
            return;
        }

        String userId = DeviceOperationManager.getUserId(request.getSession());
        LOG.debug("disable password device for user " + userId);

        try {
            this.otpOverSmsDeviceService.disable(userId, attribute);

            response.setStatus(HttpServletResponse.SC_OK);
            // notify that disable operation was successful.
            protocolContext.setSuccess(true);
        } catch (DeviceNotFoundException e) {
            LOG.debug("device not found");
        } catch (SubjectNotFoundException e) {
            LOG.debug("subject " + userId + " not found");
        } catch (DeviceRegistrationNotFoundException e) {
            LOG.error("device registration not found", e);
        }

        response.sendRedirect(this.deviceExitPath);

    }
}
