package net.link.safeonline.demo.lawyer.webapp;

import java.net.URLEncoder;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;

public class LoginPage extends Layout {

	private static final long serialVersionUID = 1L;

	public LoginPage() {
		String target = getWebRequestCycle().getWebRequest()
				.getHttpServletRequest().getRequestURL().toString();
		String redirectUrl;
		try {
			redirectUrl = LawyerApplication.SafeOnlineAuthenticationServiceUrl
					+ "?application="
					+ URLEncoder.encode(LawyerApplication.ApplicationName,
							"UTF-8") + "&target="
					+ URLEncoder.encode(target, "UTF-8");
		} catch (Exception e) {
			redirectUrl = null;
		}
		add(new Label<String>("headerTitle", "Login Page"));
		add(new ExternalLink("loginlink", redirectUrl));
	}

}
