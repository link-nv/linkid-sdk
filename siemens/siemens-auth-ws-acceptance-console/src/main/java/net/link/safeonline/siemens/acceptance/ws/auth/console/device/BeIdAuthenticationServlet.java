/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.siemens.acceptance.ws.auth.console.device;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.servlet.AbstractStatementServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Base64;


/**
 * <h2>{@link BeIdAuthenticationServlet}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 21, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class BeIdAuthenticationServlet extends AbstractStatementServlet {

    private static final Log  LOG              = LogFactory.getLog(BeIdAuthenticationServlet.class);

    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    @Override
    protected void processStatement(byte[] statementData, HttpSession session, HttpServletResponse response)
            throws ServletException, IOException {

        String authenticationStatement = Base64.encode(statementData, 0);

        LOG.debug("received authentication statement: " + authenticationStatement);

        if (null != BeIdAuthentication.getInstance()) {
            BeIdAuthentication.getInstance().authenticate(authenticationStatement);
        }

    }

}
