package net.link.safeonline.demo.cinema.webapp;

import java.net.URLEncoder;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;

public class LoginPage extends Layout {

	private static final long serialVersionUID = 1L;

	public LoginPage() {
		String target = this.getWebRequestCycle().getWebRequest()
				.getHttpServletRequest().getRequestURL().toString();
		String redirectUrl;
		try {
			redirectUrl = CinemaApplication.SafeOnlineAuthenticationServiceUrl
					+ "?application="
					+ URLEncoder.encode(CinemaApplication.ApplicationName,
							"UTF-8") + "&target="
					+ URLEncoder.encode(target, "UTF-8");
		} catch (Exception e) {
			redirectUrl = null;
		}
		add(new Label("headerTitle", "Login Page"));
		add(new ExternalLink("loginlink", redirectUrl));
	}

}
