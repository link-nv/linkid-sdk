package net.link.safeonline.util.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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

		try {
			// Execute and profile the process.
			long startTime = System.currentTimeMillis();
			chain.doFilter(request, responseWrapper);
			long duration = System.currentTimeMillis() - startTime;

			if (profileData.isLocked()) {
				LOG.debug("someone forgot to unlock the profile data");
				profileData.unlock();
			}
			try {
				HttpServletRequest httpRequest = (HttpServletRequest) request;
				String method = httpRequest.getContextPath();
				profileData.addMeasurement(method, duration);
			} catch (ProfileDataLockedException e) {
				// empty
			}

			// Add our profiling results as HTTP headers.
			for (Map.Entry<String, String> header : profileData.getHeaders()
					.entrySet())
				responseWrapper.addHeader(header.getKey(), header.getValue());
		}

		finally {
			responseWrapper.commit();
		}
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
