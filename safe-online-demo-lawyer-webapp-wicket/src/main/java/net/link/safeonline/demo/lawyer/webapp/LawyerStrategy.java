package net.link.safeonline.demo.lawyer.webapp;

import java.util.List;

import net.link.safeonline.wicket.tools.RoleSession;
import net.link.safeonline.wicket.tools.SafeOnlineStrategy;
import net.link.safeonline.wicket.tools.User;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;

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
			if (user.has("baradmin")) {
                throw new RestartResponseAtInterceptPageException(HomePage.class);
            }
			throw new RestartResponseAtInterceptPageException(ViewProfile.class);
		}
		return true;
	}

}
