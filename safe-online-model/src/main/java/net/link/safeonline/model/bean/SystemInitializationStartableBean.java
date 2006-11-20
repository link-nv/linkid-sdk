package net.link.safeonline.model.bean;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.entity.EntityEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = "SafeOnline/startup/SystemInitializationStartableBean")
public class SystemInitializationStartableBean implements Startable {

	private static final Log LOG = LogFactory
			.getLog(SystemInitializationStartableBean.class);

	private static Map<String, String> authorizedUsers;

	static {
		authorizedUsers = new HashMap<String, String>();
		authorizedUsers.put("fcorneli", "secret");
		authorizedUsers.put("dieter", "secret");
		authorizedUsers.put("mario", "secret");
	}

	@EJB
	private EntityDAO entityDAO;

	public void start() {
		LOG.debug("start");
		for (Map.Entry<String, String> authorizedUser : authorizedUsers
				.entrySet()) {
			String username = authorizedUser.getKey();
			EntityEntity entity = this.entityDAO.findEntity(username);
			if (null != entity) {
				continue;
			}
			String password = authorizedUser.getValue();
			this.entityDAO.addEntity(username, password);
		}
	}

	public void stop() {
		LOG.debug("stop");
	}
}
