package net.link.safeonline.demo.lawyer.webapp;

import wicket.Session;
import wicket.protocol.http.WebApplication;

public class LawyerApplication extends WebApplication {

	public static String SafeOnlineAuthenticationServiceUrl;
	
	public static String ApplicationName;
	
	@Override
	protected void init() {
		setSessionFactory(this);
		getSecuritySettings().setAuthorizationStrategy(new LawyerStrategy());
		SafeOnlineAuthenticationServiceUrl = getWicketServlet().getInitParameter("SafeOnlineAuthenticationServiceUrl");
		ApplicationName = getWicketServlet().getInitParameter("ApplicationName");
	}

	@Override
	public Class<?> getHomePage() {
		return HomePage.class;
	}

	@Override
	public Session newSession() {
		return new LawyerSession(this);
	}

}
