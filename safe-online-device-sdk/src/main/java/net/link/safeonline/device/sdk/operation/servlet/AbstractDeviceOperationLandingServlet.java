/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.operation.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.common.SafeOnlineAppConstants;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.device.sdk.exception.DeviceFinalizationException;
import net.link.safeonline.device.sdk.exception.DeviceInitializationException;
import net.link.safeonline.device.sdk.operation.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.operation.saml2.Saml2Handler;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.ServletUtils;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Landing servlet on the remote device issuer side where OLAS posts its SAML authentication request to for device registration, updating,
 * removal.
 * 
 * @author wvdhaute
 * 
 */
public abstract class AbstractDeviceOperationLandingServlet extends AbstractInjectionServlet {

    private static final long  serialVersionUID  = 1L;
    private static final Log   LOG               = LogFactory.getLog(AbstractDeviceOperationLandingServlet.class);

    /**
     * PATH within the device web application that provides the device registration service. <i>[optional, default: Abort request as not
     * supported]</i>
     */
    public static final String REGISTRATION_PATH = "RegistrationPath";

    /**
     * PATH within the device web application that removes a registered device. <i>[optional, default: Abort request as not supported]</i>
     */
    public static final String REMOVAL_PATH      = "RemovalPath";

    /**
     * PATH within the device web application that updates a registered device. <i>[optional, default: Abort request as not supported]</i>
     */
    public static final String UPDATE_PATH       = "UpdatePath";

    /**
     * PATH within the device web application that disables a registered device. <i>[optional, default: Abort request as not supported]</i>
     */
    public static final String DISABLE_PATH      = "DisablePath";

    /**
     * PATH within the device web application that enabled a disabled device. <i>[optional, default: Abort request as not supported]</i>
     */
    public static final String ENABLE_PATH       = "EnablePath";

    /**
     * PATH within the device web application to redirect to when a protocol error occurs. <i>[optional, default: A simple HTML page
     * containing the error message]</i>
     */
    public static final String ERROR_PAGE        = "ErrorPage";

    @Init(name = REGISTRATION_PATH, optional = true)
    private String             registrationPath;

    @Init(name = REMOVAL_PATH, optional = true)
    private String             removalPath;

    @Init(name = UPDATE_PATH, optional = true)
    private String             updatePath;

    @Init(name = DISABLE_PATH, optional = true)
    private String             disablePath;

    @Init(name = ENABLE_PATH, optional = true)
    private String             enablePath;

    @Init(name = ERROR_PAGE, optional = true)
    private String             errorPage;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doPost");

        /*
         * Set the language cookie if language was specified in the browser post
         */
        HttpSession session = request.getSession();
        String reqLang = request.getParameter("Language");
        String reqCol = request.getParameter("Color");
        String reqMin = request.getParameter("Minimal");
        Locale language = reqLang == null || reqLang.length() == 0? null: new Locale(reqLang);
        Integer color = reqCol == null || reqCol.length() == 0? null: Integer.decode(reqCol);
        Boolean minimal = reqMin == null || reqMin.length() == 0? null: Boolean.parseBoolean(reqMin);

        if (null != language) {
            Cookie languageCookie = new Cookie(SafeOnlineCookies.LANGUAGE_COOKIE, language.getLanguage());
            languageCookie.setPath("/");
            languageCookie.setMaxAge(60 * 60 * 24 * 30 * 6);
            response.addCookie(languageCookie);
        }
        if (null != minimal && minimal) {
            session.setAttribute(SafeOnlineAppConstants.COLOR_SESSION_ATTRIBUTE, color);
            session.setAttribute(SafeOnlineAppConstants.MINIMAL_SESSION_ATTRIBUTE, minimal);
        }

        /*
         * Figure out what the request wants us to do.
         */
        OlasKeyStore nodeKeyStore = getKeyStore();
        DeviceOperationType deviceOperation;
        try {
            Saml2Handler handler = Saml2Handler.getSaml2Handler(request);
            handler.init(getIssuer(), nodeKeyStore.getCertificate(), nodeKeyStore.getKeyPair());
            deviceOperation = handler.initDeviceOperation(request);
            if (deviceOperation.equals(DeviceOperationType.REGISTER) || deviceOperation.equals(DeviceOperationType.NEW_ACCOUNT_REGISTER)) {
                if (null == registrationPath) {
                    handler.abortDeviceOperation(request, response);
                }
                response.sendRedirect(registrationPath);
            } else if (deviceOperation.equals(DeviceOperationType.REMOVE)) {
                if (null == removalPath) {
                    handler.abortDeviceOperation(request, response);
                }
                response.sendRedirect(removalPath);
            } else if (deviceOperation.equals(DeviceOperationType.UPDATE)) {
                if (null == updatePath) {
                    handler.abortDeviceOperation(request, response);
                }
                response.sendRedirect(updatePath);
            } else if (deviceOperation.equals(DeviceOperationType.DISABLE)) {
                if (null == disablePath) {
                    handler.abortDeviceOperation(request, response);
                }
                response.sendRedirect(disablePath);
            } else if (deviceOperation.equals(DeviceOperationType.ENABLE)) {
                if (null == enablePath) {
                    handler.abortDeviceOperation(request, response);
                }
                response.sendRedirect(enablePath);
            } else {
                handler.abortDeviceOperation(request, response);
            }
        } catch (DeviceInitializationException e) {
            LOG.debug("device initialization exception: " + e.getMessage());
            ServletUtils.redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(e.getMessage()));

            return;
        } catch (DeviceFinalizationException e) {
            LOG.debug("device finalization exception: " + e.getMessage());
            ServletUtils.redirectToErrorPage(request, response, errorPage, null, new ErrorMessage(e.getMessage()));

            return;
        }
    }

    /**
     * @return The issuer of the signing keys. For OLAS nodes, this is the node name.
     */
    protected abstract String getIssuer();

    /**
     * @return The signing keystore. For OLAS nodes, this is the node keystore.
     */
    protected abstract OlasKeyStore getKeyStore();
}
