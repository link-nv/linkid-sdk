package net.link.safeonline.listener;

import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.link.safeonline.Startable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This servlet context listener will start and stop all components registered
 * in JNDI under the prefix defined by the web.xml context-param
 * StartableJndiPrefix.
 * 
 * @author fcorneli
 * 
 */
public class StartupServletContextListener implements ServletContextListener {

	private static final Log LOG = LogFactory
			.getLog(StartupServletContextListener.class);

	public void contextInitialized(ServletContextEvent event) {
		LOG.debug("context initialized");
		List<Startable> startables = getStartables(event);
		for (Startable startable : startables) {
			LOG.debug("starting: " + startable);
			try {
				startable.start();
			} catch (Exception e) {
				LOG.error("error starting: " + e.getMessage(), e);
				throw new RuntimeException("error starting: " + e.getMessage(),
						e);
			}
		}
	}

	public void contextDestroyed(ServletContextEvent event) {
		LOG.debug("context destroyed");
		List<Startable> startables = getStartables(event);
		for (Startable startable : startables) {
			LOG.debug("stopping: " + startable);
			startable.stop();
		}
	}

	private List<Startable> getStartables(ServletContextEvent event) {
		ServletContext servletContext = event.getServletContext();
		String startableJnidPrefix = servletContext
				.getInitParameter("StartableJndiPrefix");
		LOG.debug("get startables at " + startableJnidPrefix);
		try {
			InitialContext initialContext = new InitialContext();
			Context context = (Context) initialContext
					.lookup(startableJnidPrefix);
			NamingEnumeration<NameClassPair> result = initialContext
					.list(startableJnidPrefix);
			List<Startable> startables = new LinkedList<Startable>();
			while (result.hasMore()) {
				NameClassPair nameClassPair = result.next();
				String objectName = nameClassPair.getName();
				LOG.debug(objectName + ":" + nameClassPair.getClassName());
				Object object = context.lookup(objectName);
				if (!(object instanceof Startable)) {
					String message = "object \"" + startableJnidPrefix + "/"
							+ objectName + "\" is not a "
							+ Startable.class.getName();
					LOG.error(message);
					throw new IllegalStateException(message);
				}
				Startable startable = (Startable) object;
				startables.add(startable);
			}
			return startables;
		} catch (NamingException e) {
			throw new RuntimeException("naming error: " + e.getMessage(), e);
		}
	}
}
