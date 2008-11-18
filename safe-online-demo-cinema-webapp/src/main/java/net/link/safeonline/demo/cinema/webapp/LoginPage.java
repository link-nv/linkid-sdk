package net.link.safeonline.demo.cinema.webapp;

import javax.ejb.EJB;
import javax.servlet.ServletException;

import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.demo.cinema.service.UserService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.demo.wicket.web.OlasLoginLink;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;


public class LoginPage extends LayoutPage {

    private static final long serialVersionUID = 1L;

    @EJB(mappedName = UserService.JNDI_BINDING)
    transient UserService     userService;


    /**
     * If the user is logged in; continue to the ticket history page.
     * 
     * If not, show a link to the OLAS authentication service for logging the user in.
     */
    public LoginPage() {

        // If logged in, send user to the ticket history page.
        if (WicketUtil.isAuthenticated(getRequest())) {
            try {
                CinemaUserEntity user = this.userService.getUser(WicketUtil.getUserId(getRequest()));
                user = this.userService.updateUser(user, WicketUtil.toServletRequest(getRequest()));
                CinemaSession.get().setUser(user);

                throw new RestartResponseException(TicketPage.class);
            }

            catch (ServletException e) {
                this.LOG.error("LoginManager claimed we were logged in but no user id was available!", e);
            }
        }

        add(new Label("headerTitle", "Login Page"));
        add(new OlasLoginLink("loginLink"));
    }
}
