/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * From here we continue doing java runtime version checking.
 * 
 * @author fcorneli
 * 
 */
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(RegisterServlet.class);


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        prepareJavaVersionCheck(req, resp);
    }

    private void prepareJavaVersionCheck(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        LOG.debug("prepareJavaVersionCheck");
        HttpSession session = req.getSession();
        JavaVersionServlet.setPkcs11Target("register-beid.seam", session);
        JavaVersionServlet.setJava15NoPkcs11Target("missing-middleware.seam", session);
        JavaVersionServlet.setJava16NoPkcs11Target("register-beid-pcsc.seam", session);
        LOG.debug("redirecting to beid.html for Java runtime version checking...");
        resp.sendRedirect("beid.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        prepareJavaVersionCheck(req, resp);
    }
}
