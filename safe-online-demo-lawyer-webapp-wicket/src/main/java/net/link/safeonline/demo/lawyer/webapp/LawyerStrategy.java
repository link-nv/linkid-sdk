package net.link.safeonline.demo.lawyer.webapp;

import java.util.List;

import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import net.link.safeonline.demo.wicket.tools.RoleSession;
import net.link.safeonline.demo.wicket.tools.SafeOnlineStrategy;
import net.link.safeonline.demo.wicket.tools.User;

public class LawyerStrategy extends SafeOnlineStrategy {

	public LawyerStrategy() {
		super(LoginPage.class);
	}

    @Override
    @SuppressWarnings("unchecked")
	public boolean isInstantiationAuthorized(final Class page) {
		if (!super.isInstantiationAuthorized(page)) {
			RoleSession session = (RoleSession) Session.get();
			User user = session.getUser();
			if (user.getUsername().equals("dieter")) {
				List<String> roles = user.getRoles();
				roles.add("baradmin");
				user.setRoles(roles);
			}
			if (super.isInstantiationAuthorized(page)) return true;
			if (user.has("baradmin"))
				throw new RestartResponseAtInterceptPageException(HomePage.class);
			throw new RestartResponseAtInterceptPageException(ViewProfile.class);
		}
		return true;
	}

}
