package net.link.safeonline.encap.servlet;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationManager;
import net.link.safeonline.model.encap.EncapDeviceService;
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

    private static final long  serialVersionUID = 1L;

    private static final Log   LOG              = LogFactory.getLog(RemoveServlet.class);

    @Init(name = "RemovePath")
    private String             removePath;

    @Init(name = "DeviceExitPath")
    private String             deviceExitPath;

    @EJB(mappedName = EncapDeviceService.JNDI_BINDING)
    private EncapDeviceService encapDeviceService;


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

        String attribute = DeviceOperationManager.findAttribute(request.getSession());
        if (null == attribute) {
            response.sendRedirect(this.removePath);
            return;
        }
        String userId = DeviceOperationManager.getUserId(request.getSession());
        LOG.debug("remove encap device: " + attribute + " for user " + userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(request.getSession());
        protocolContext.setSuccess(false);

        try {
            this.encapDeviceService.remove(userId, attribute);

            response.setStatus(HttpServletResponse.SC_OK);
            // notify that disable operation was successful.
            protocolContext.setSuccess(true);
        } catch (SubjectNotFoundException e) {
            LOG.debug("subject " + userId + " not found");
        } catch (MobileException e) {
            LOG.debug("mobile exception thrown", e);
        } catch (AttributeTypeNotFoundException e) {
            LOG.debug("AttributeTypeNotFoundException", e);
        } catch (PermissionDeniedException e) {
            LOG.debug("PermissionDeniedException", e);
        } catch (AttributeNotFoundException e) {
            LOG.debug("AttributeNotFoundException", e);
        }

        response.sendRedirect(this.deviceExitPath);

    }
}
