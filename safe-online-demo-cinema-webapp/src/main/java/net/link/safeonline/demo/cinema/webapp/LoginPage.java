package net.link.safeonline.demo.cinema.webapp;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.demo.cinema.entity.UserEntity;
import net.link.safeonline.demo.cinema.service.UserService;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;

public class LoginPage extends LayoutPage {

	private static final long serialVersionUID = 1L;

	@EJB
	transient UserService userService;

	/**
	 * If the user is logged in; continue to the ticket history page.
	 * 
	 * If not, show a link to the OLAS authentication service for logging the
	 * user in.
	 */
	public LoginPage() {

		// If logged in, send user to the ticket history page.
		HttpServletRequest loginRequest = ((WebRequest) getRequest())
				.getHttpServletRequest();
		if (LoginManager.isAuthenticated(loginRequest)) {
			try {
				UserEntity user = this.userService.getUser(LoginManager
						.getUsername(loginRequest));
				this.userService.updateUser(user, loginRequest);
				CinemaSession.get().setUser(user);

				setResponsePage(TicketPage.class);
				return;
			}

			catch (ServletException e) {
				this.LOG
						.error(
								"LoginManager claimed we were logged in but no user id was available!",
								e);
			}
		}

		add(new Label<String>("headerTitle", "Login Page"));
		add(new Link<Object>("loginlink") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {

				getRequestCycle().setRequestTarget(new IRequestTarget() {

					public void detach(RequestCycle requestCycle) {

					}

					public void respond(RequestCycle requestCycle) {

						HttpServletRequest request = ((WebRequest) requestCycle
								.getRequest()).getHttpServletRequest();
						HttpServletResponse response = ((WebResponse) requestCycle
								.getResponse()).getHttpServletResponse();
						String target = request.getServletPath();

						SafeOnlineLoginUtils.login(target, request, response);
					}

				});
			}
		});
	}
}
