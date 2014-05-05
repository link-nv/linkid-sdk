/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.ws;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.util.logging.Logger;


public class ExampleRestartServlet extends HttpServlet {

    private static final Logger logger = Logger.get( ExampleRestartServlet.class );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.dbg( "doGet" );

        request.getSession().invalidate();
        response.sendRedirect( "./ws" );
    }
}