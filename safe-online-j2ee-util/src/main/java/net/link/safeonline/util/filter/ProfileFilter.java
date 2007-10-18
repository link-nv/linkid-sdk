package net.link.safeonline.util.filter;

import static net.link.safeonline.util.filter.ProfileStats.REQUEST_TIME;

import java.io.IOException;
import java.util.Map;

import javax.security.jacc.PolicyContext;
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

	public static final String PROFILING_ENABLED = "ProfilingEnabled";

	private static final Log LOG = LogFactory.getLog(ProfileFilter.class);

	public void destroy() {

		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		// Buffer the response so we can add our own headers.
		LOG.debug("profiling enabled");
		BufferedServletResponseWrapper responseWrapper = new BufferedServletResponseWrapper(
				(HttpServletResponse) response);

		try {
			// Execute and profile the process.
			long startTime = System.currentTimeMillis();
			chain.doFilter(request, responseWrapper);
			long duration = System.currentTimeMillis() - startTime;

			// Add our profiling results as HTTP headers.
			LOG.debug("Setting HEADER: " + REQUEST_TIME.getHeader() + "  =>  "
					+ String.valueOf(duration));
			responseWrapper.addHeader(REQUEST_TIME.getHeader(), String
					.valueOf(duration));
			try {
				ProfileData profileData = (ProfileData) PolicyContext
						.getContext(ProfileData.KEY);
				for (Map.Entry<String, String> header : profileData
						.getHeaders().entrySet()) {
					LOG.debug("Setting HEADER: " + header.getKey() + "  =>  "
							+ header.getValue());
					responseWrapper.addHeader(header.getKey(), header
							.getValue());
				}
			}

			catch (Exception e) {
				LOG.error("Couldn't retrieve profile data from JACC.", e);
			}
		}

		finally {
			responseWrapper.commit();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(FilterConfig filterConfig) {

		/* Nothing to do. */
	}
}
