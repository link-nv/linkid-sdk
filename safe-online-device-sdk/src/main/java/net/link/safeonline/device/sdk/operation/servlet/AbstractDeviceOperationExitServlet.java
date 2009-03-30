package net.link.safeonline.device.sdk.operation.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.exception.DeviceFinalizationException;
import net.link.safeonline.device.sdk.operation.saml2.Saml2Handler;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.ServletUtils;
import net.link.safeonline.util.servlet.annotation.Init;


/**
 * This servlet returns a saml authentication response from device issuer to OLAS, notifying the status of the device operation.
 * 
 * @author wvdhaute
 * 
 */
public abstract class AbstractDeviceOperationExitServlet extends AbstractInjectionServlet {

    private static final long serialVersionUID = 1L;

    @Init(name = "ErrorPage", optional = true)
    private String            errorPage;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke(request, response);
    }

    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke(request, response);
    }

    private void invoke(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Saml2Handler handler = Saml2Handler.findSaml2Handler(request);
        if (null == handler) {
            /*
             * If no protocol handler is active at this point then something must be going wrong here.
             */
            ServletUtils.redirectToErrorPage(request, response, errorPage, null, new ErrorMessage("No protocol handler is active"));
            return;

        }
        try {
            handler.finalizeDeviceOperation(request, response);
        } catch (DeviceFinalizationException e) {
            ServletUtils.redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(e.getMessage()));
            return;
        }
    }
}
