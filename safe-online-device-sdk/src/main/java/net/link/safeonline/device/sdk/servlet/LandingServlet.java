/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.servlet;

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
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.saml2.Saml2Handler;
import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
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
public abstract class LandingServlet extends AbstractInjectionServlet {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(LandingServlet.class);

    @Init(name = "ServletEndpointUrl", optional = true)
    private String            servletEndpointUrl;

    @Init(name = "RegistrationUrl", optional = true)
    private String            registrationUrl;

    @Init(name = "RemovalUrl", optional = true)
    private String            removalUrl;

    @Init(name = "UpdateUrl", optional = true)
    private String            updateUrl;

    @Init(name = "DisableUrl", optional = true)
    private String            disableUrl;

    @Init(name = "EnableUrl", optional = true)
    private String            enableUrl;

    @Init(name = "ErrorPage", optional = true)
    private String            errorPage;


    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doPost");

        /**
         * Wrap the request to use the servlet endpoint url if defined. To prevent failure when behind a reverse proxy or loadbalancer when
         * opensaml is checking the destination field.
         */
        HttpServletRequestEndpointWrapper requestWrapper;
        if (null != servletEndpointUrl) {
            requestWrapper = new HttpServletRequestEndpointWrapper(request, servletEndpointUrl);
        } else {
            requestWrapper = new HttpServletRequestEndpointWrapper(request, request.getRequestURL().toString());
        }

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
            session.setAttribute(SafeOnlineAppConstants.COLOR_ATTRIBUTE, color);
            session.setAttribute(SafeOnlineAppConstants.MINIMAL_ATTRIBUTE, minimal);
        }

        /*
         * Figure out what the request wants us to do.
         */
        OlasKeyStore nodeKeyStore = getKeyStore();
        DeviceOperationType deviceOperation;
        try {
            Saml2Handler handler = Saml2Handler.getSaml2Handler(requestWrapper);
            handler.init(configParams, getIssuer(), nodeKeyStore.getCertificate(), nodeKeyStore.getKeyPair());
            deviceOperation = handler.initDeviceOperation(requestWrapper);
            if (deviceOperation.equals(DeviceOperationType.REGISTER) || deviceOperation.equals(DeviceOperationType.NEW_ACCOUNT_REGISTER)) {
                if (null == registrationUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(registrationUrl);
            } else if (deviceOperation.equals(DeviceOperationType.REMOVE)) {
                if (null == removalUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(removalUrl);
            } else if (deviceOperation.equals(DeviceOperationType.UPDATE)) {
                if (null == updateUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(updateUrl);
            } else if (deviceOperation.equals(DeviceOperationType.DISABLE)) {
                if (null == disableUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(disableUrl);
            } else if (deviceOperation.equals(DeviceOperationType.ENABLE)) {
                if (null == enableUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(enableUrl);
            } else {
                handler.abortDeviceOperation(requestWrapper, response);
            }
        } catch (DeviceInitializationException e) {
            LOG.debug("device initialization exception: " + e.getMessage());
            redirectToErrorPage(requestWrapper, response, errorPage, null, new ErrorMessage(e.getMessage()));

            return;
        } catch (DeviceFinalizationException e) {
            LOG.debug("device finalization exception: " + e.getMessage());
            redirectToErrorPage(requestWrapper, response, errorPage, null, new ErrorMessage(e.getMessage()));

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
