package net.link.safeonline.sdk.example.mobile.x509;

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
