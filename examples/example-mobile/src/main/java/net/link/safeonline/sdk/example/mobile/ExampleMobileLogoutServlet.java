/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.example.mobile;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;


public class ExampleMobileLogoutServlet extends HttpServlet {

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession( false );
        if (session != null)
            session.invalidate();

        response.sendRedirect( "./" );
    }
}
