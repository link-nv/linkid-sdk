/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import static net.link.safeonline.beid.webapp.BeIdMountPoints.AuthenticationType.PCSC;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.NO_MIDDLEWARE;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.MountPoint.AUTHENTICATION;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.MountPoint.ERROR;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.beid.servlet.JavaVersionServlet.JAVA_VERSION;
import net.link.safeonline.model.node.util.AbstractNodeInjectionServlet;
import net.link.safeonline.util.servlet.annotation.In;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Servlet that makes a decision in case the client-side applet did not detect a PKCS#11 library.
 * 
 * @author fcorneli
 * 
 */
public class NoPkcs11Servlet extends AbstractNodeInjectionServlet {

    private static final Log  LOG              = LogFactory.getLog(NoPkcs11Servlet.class);

    private static final long serialVersionUID = 1L;

    @In(value = JavaVersionServlet.JAVA_VERSION_NAME, required = true)
    private JAVA_VERSION      javaVersion;


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke(request, response);
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke(request, response);
    }

    private void invoke(@SuppressWarnings("unused") HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        LOG.debug("java version: " + javaVersion);
        if (javaVersion == JAVA_VERSION.JAVA_1_6) {
            response.sendRedirect(AUTHENTICATION.linkFor(PCSC));
        } else {
            response.sendRedirect(ERROR.linkFor(NO_MIDDLEWARE));
        }
    }
}