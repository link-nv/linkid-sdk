/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.auth.protocol.saml2.Saml2PostProtocolHandler;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.ProtocolContext;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.helpdesk.HelpdeskLogger;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Manager class for the protocol handlers registered within the authentication web application.
 * 
 * @author fcorneli
 * 
 */
public class ProtocolHandlerManager {

    public static final String PROTOCOL_HANDLER_ID_ATTRIBUTE              = ProtocolHandlerManager.class.getName() + ".ProtocolHandlerName";

    public static final String PROTOCOL_DONT_INVALIDATE_SESSION_ATTRIBUTE = ProtocolHandlerManager.class.getName()
                                                                                  + ".DontInvalidateSession";

    private static final Log   LOG                                        = LogFactory.getLog(ProtocolHandlerManager.class);


    private ProtocolHandlerManager() {

        // empty
    }


    private static final List<ProtocolHandler>        protocolHandlers   = new LinkedList<ProtocolHandler>();

    private static final Map<String, ProtocolHandler> protocolHandlerMap = new HashMap<String, ProtocolHandler>();

    static {
        registerProtocolHandler(Saml2PostProtocolHandler.class);
    }


    private static void registerProtocolHandler(Class<? extends ProtocolHandler> protocolHandlerClass) {

        try {
            ProtocolHandler protocolHandler = protocolHandlerClass.newInstance();
            String protocolId = protocolHandlerClass.getName();
            if (protocolHandlerMap.containsKey(protocolId))
                throw new RuntimeException("protocol handler already registered for Id: " + protocolId);
            protocolHandlerMap.put(protocolId, protocolHandler);
            protocolHandlers.add(protocolHandler);
        } catch (Exception e) {
            throw new RuntimeException("could not initialize protocol handler: " + protocolHandlerClass.getName() + "; message: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Handles the authentication protocol request. This method return a protocol context in case of a successful initiation of the
     * authentication procedure. The method returns <code>null</code> if no appropriate authentication protocol handler has been found.
     * 
     * @param request
     * @return a protocol context or <code>null</code>.
     * @throws ProtocolException
     *             in case of a protocol error.
     */
    public static ProtocolContext handleRequest(HttpServletRequest request)
            throws ProtocolException {

        String reqLang = request.getParameter("Language");
        String reqCol = request.getParameter("Color");
        String reqMin = request.getParameter("Minimal");
        Locale language = reqLang == null || reqLang.length() == 0? null: new Locale(reqLang);
        Integer color = reqCol == null || reqCol.length() == 0? null: Integer.decode(reqCol);
        Boolean minimal = reqMin == null || reqMin.length() == 0? null: Boolean.parseBoolean(reqMin);

        for (ProtocolHandler protocolHandler : protocolHandlers) {
            LOG.debug("trying protocol handler: " + protocolHandler.getClass().getSimpleName());
            ProtocolContext protocolContext;
            try {
                protocolContext = protocolHandler.handleRequest(request, language, color, minimal);
            } catch (ProtocolException e) {
                String protocolName = protocolHandler.getName();
                e.setProtocolName(protocolName);
                SecurityAuditLogger securityAuditLogger = EjbUtils.getEJB(SecurityAuditLogger.JNDI_BINDING, SecurityAuditLogger.class);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "Protocol: " + protocolName + " : " + e.getMessage());
                throw e;
            }
            if (null != protocolContext) {
                HttpSession session = request.getSession();
                String protocolId = protocolHandler.getClass().getName();
                session.setAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE, protocolId);
                return protocolContext;
            }
        }
        return null;
    }

    /**
     * Handles the authentication response according to the authentication protocol by which the current authentication procedure was
     * initiated.
     * 
     * @param session
     * @param response
     * @throws ProtocolException
     */
    public static void authnResponse(HttpSession session, HttpServletResponse response)
            throws ProtocolException {

        String protocolId = (String) session.getAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE);
        if (null == protocolId)
            throw new ProtocolException("incorrect request handling detected");
        ProtocolHandler protocolHandler = protocolHandlerMap.get(protocolId);
        if (null == protocolHandler)
            throw new ProtocolException("unsupported protocol for protocol Id: " + protocolId);

        try {
            protocolHandler.authnResponse(session, response);
        } catch (ProtocolException e) {
            String protocolName = protocolHandler.getName();
            e.setProtocolName(protocolName);
            throw e;
        }

        HelpdeskLogger.clear(session);

        /*
         * It's important to invalidate the session here. Else we spill resources and we prevent a user to login twice since the
         * authentication service instance was already removed from the session context.
         */
        session.invalidate();
    }

    /**
     * Handles the logout request. This method returns a logout protocol context in case of a successful initiation of the single logout
     * procedure. The method returns <code>null</code> if no appropriate logout protocol handler has been found.
     * 
     * @param request
     * @return a logout protocol context or <code>null</code>.
     * @throws ProtocolException
     *             in case of a protocol error.
     */
    public static LogoutProtocolContext handleLogoutRequest(HttpServletRequest request)
            throws ProtocolException {

        for (ProtocolHandler protocolHandler : protocolHandlers) {
            LOG.debug("trying protocol handler: " + protocolHandler.getClass().getSimpleName());
            LogoutProtocolContext logoutProtocolContext;
            try {
                logoutProtocolContext = protocolHandler.handleLogoutRequest(request);
            } catch (ProtocolException e) {
                String protocolName = protocolHandler.getName();
                e.setProtocolName(protocolName);
                SecurityAuditLogger securityAuditLogger = EjbUtils.getEJB(SecurityAuditLogger.JNDI_BINDING, SecurityAuditLogger.class);
                securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, "Protocol: " + protocolName + " : " + e.getMessage());
                throw e;
            }
            if (null != logoutProtocolContext) {
                HttpSession session = request.getSession();
                String protocolId = protocolHandler.getClass().getName();
                session.setAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE, protocolId);
                return logoutProtocolContext;
            }
        }
        return null;
    }

    /**
     * Handles the logout response. This method returns the application name in the response if logout was successful. Else the method
     * returns <code>null</code>.
     * 
     * @param request
     * @return application name or <code>null</code>.
     * @throws ProtocolException
     *             in case of a protocol error.
     */
    public static String handleLogoutResponse(HttpServletRequest request)
            throws ProtocolException {

        HttpSession session = request.getSession();
        String protocolId = (String) session.getAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE);
        if (null == protocolId)
            throw new ProtocolException("incorrect request handling detected");
        ProtocolHandler protocolHandler = protocolHandlerMap.get(protocolId);
        if (null == protocolHandler)
            throw new ProtocolException("unsupported protocol for protocol Id: " + protocolId);

        try {
            return protocolHandler.handleLogoutResponse(request);
        } catch (ProtocolException e) {
            String protocolName = protocolHandler.getName();
            e.setProtocolName(protocolName);
            throw e;
        }
    }

    /**
     * Send out a logout request to the specified application
     * 
     * @param application
     * @param session
     * @param response
     * @throws ProtocolException
     */
    public static void logoutRequest(ApplicationEntity application, HttpSession session, HttpServletResponse response)
            throws ProtocolException {

        String protocolId = (String) session.getAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE);
        if (null == protocolId)
            throw new ProtocolException("incorrect request handling detected");
        ProtocolHandler protocolHandler = protocolHandlerMap.get(protocolId);
        if (null == protocolHandler)
            throw new ProtocolException("unsupported protocol for protocol Id: " + protocolId);

        try {
            protocolHandler.logoutRequest(application, session, response);
        } catch (ProtocolException e) {
            String protocolName = protocolHandler.getName();
            e.setProtocolName(protocolName);
            throw e;
        }
    }

    /**
     * Handles the logout response according to the authentication protocol by which the current logout procedure was initiated.
     * 
     * @param partialLogout
     * @param target
     * @param session
     * @param response
     * @throws ProtocolException
     */
    public static void logoutResponse(boolean partialLogout, String target, HttpSession session, HttpServletResponse response)
            throws ProtocolException {

        String protocolId = (String) session.getAttribute(PROTOCOL_HANDLER_ID_ATTRIBUTE);
        if (null == protocolId)
            throw new ProtocolException("incorrect request handling detected");
        ProtocolHandler protocolHandler = protocolHandlerMap.get(protocolId);
        if (null == protocolHandler)
            throw new ProtocolException("unsupported protocol for protocol Id: " + protocolId);

        try {
            protocolHandler.logoutResponse(partialLogout, target, session, response);
        } catch (ProtocolException e) {
            String protocolName = protocolHandler.getName();
            e.setProtocolName(protocolName);
            throw e;
        }

        /*
         * It's important to invalidate the session here. Else we spill resources and we prevent a user to login twice since the
         * authentication service instance was already removed from the session context.
         */
        session.invalidate();

    }

}
