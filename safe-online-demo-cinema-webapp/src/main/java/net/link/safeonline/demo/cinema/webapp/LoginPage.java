package net.link.safeonline.demo.cinema.webapp;

import net.link.safeonline.wicket.web.OlasLoginLink;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;


public class LoginPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * If the user is logged in; continue to the ticket history page.
     * 
     * If not, show a link to the OLAS authentication service for logging the user in.
     */
    public LoginPage() {

        // If logged in, send user to the ticket history page.
        if (CinemaSession.get().isUserSet())
            throw new RestartResponseException(TicketPage.class);

        add(new Label("headerTitle", "Login Page"));
        add(new OlasLoginLink("loginLink"));
    }
}
