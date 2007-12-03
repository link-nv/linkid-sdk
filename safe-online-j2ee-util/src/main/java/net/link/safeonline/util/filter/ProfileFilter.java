package net.link.safeonline.util.filter;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.ee.BufferedServletResponseWrapper;
import net.link.safeonline.util.jacc.ProfileData;
import net.link.safeonline.util.jacc.ProfileDataLockedException;
import net.link.safeonline.util.jacc.ProfilingPolicyContextHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet Filter profiles the request and adds the results as headers of the
 * response.<br>
 * 
 * @author mbillemo
 * 
 */
public class ProfileFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(ProfileFilter.class);

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// Only attempt to profile HTTP requests.
		if (!(response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}

		LOG.debug("Enabling profiler.");
		ProfileData profileData = new ProfileData();
		// publish the profile data on JACC
		ProfilingPolicyContextHandler.setProfileData(profileData);

		// Buffer the response so we can add our own headers.
		BufferedServletResponseWrapper responseWrapper = new BufferedServletResponseWrapper(
				(HttpServletResponse) response);

		long startFreeMem = getFreeMemory();
		long startTime = System.currentTimeMillis();

		try {
			try {
				chain.doFilter(request, responseWrapper);
			}

			finally {
				long deltaTime = System.currentTimeMillis() - startTime;
				long endFreeMem = getFreeMemory();
				long usedMem = startFreeMem - endFreeMem;

				try {
					profileData.addMeasurement(ProfileData.REQUEST_START_TIME,
							startTime);
					profileData.addMeasurement(ProfileData.REQUEST_DELTA_TIME,
							deltaTime);
					profileData.addMeasurement(ProfileData.REQUEST_USED_MEM,
							usedMem);
					profileData.addMeasurement(ProfileData.REQUEST_FREE_MEM,
							endFreeMem);
				} catch (ProfileDataLockedException e) {
				}

				// Add our profiling results as HTTP headers.
				for (Map.Entry<String, String> header : profileData
						.getHeaders().entrySet())
					responseWrapper.addHeader(header.getKey(), header
							.getValue());

				if (profileData.isLocked()) {
					LOG.debug("someone forgot to unlock the profile data");
					profileData.unlock();
				}
			}
		}

		catch (Exception e) {
			throw new ProfiledException(e, profileData.getHeaders());
		}

		finally {
			responseWrapper.commit();
		}
	}

	private long getFreeMemory() {

		long free = 0;
		try {
			MBeanServerConnection rmi = (MBeanServerConnection) getInitialContext()
					.lookup("jmx/invoker/RMIAdaptor");
			free = (Long) rmi.getAttribute(new ObjectName(
					"jboss.system:type=ServerInfo"), "FreeMemory");
		} catch (Exception e) {
			LOG.error("Failed to read in free memory through JMX.", e);
		}

		return free;
	}

	private static InitialContext getInitialContext() throws NamingException {

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		environment.put(Context.PROVIDER_URL, "localhost:1099");

		return new InitialContext(environment);
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(@SuppressWarnings("unused")
	FilterConfig filterConfig) {
		// empty
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		// empty
	}
}
