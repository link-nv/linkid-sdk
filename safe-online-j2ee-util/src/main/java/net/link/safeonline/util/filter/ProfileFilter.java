package net.link.safeonline.util.filter;

import static net.link.safeonline.util.filter.ProfileStats.REQUEST_TIME;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.ee.BufferedServletResponseWrapper;
import net.link.safeonline.util.jacc.ProfileData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet Filter profiles the request and adds the results as headers of the
 * response.<br>
 * <br>
 * One header identifies the total request processing time and multiple other
 * headers go into detail about the calls that were made onto the model. The
 * data for these headers is collected from the JACC Context as a
 * {@link ProfileData} object.
 * 
 * @author mbillemo
 * 
 */
public class ProfileFilter implements Filter {

	private static final Log LOG = LogFactory.getLog(ProfileFilter.class);

	private ProfileData profileData;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// Only attempt to profile HTTP requests.
		if (!(response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}

		// Start collecting data. This clears and enables the profiler.
		LOG.debug("Enabling profiler.");
		profileData.start();

		// Buffer the response so we can add our own headers.
		BufferedServletResponseWrapper responseWrapper = new BufferedServletResponseWrapper(
				(HttpServletResponse) response);

		try {
			// Execute and profile the process.
			long startTime = System.currentTimeMillis();
			chain.doFilter(request, responseWrapper);
			long duration = System.currentTimeMillis() - startTime;

			// Assign global statistics to profiling data.
			profileData.setStatistic(REQUEST_TIME, duration);

			// Add our profiling results as HTTP headers.
			for (Map.Entry<String, String> header : profileData.getHeaders()
					.entrySet())
				responseWrapper.addHeader(header.getKey(), header.getValue());
		}

		finally {
			responseWrapper.commit();
			profileData.stop();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(FilterConfig filterConfig) {

		profileData = ProfileData.getProfileData();
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {

		/* Aargh. */
	}
}
