package net.link.safeonline.util.webapp.filter;

import static net.link.safeonline.util.webapp.filter.ProfileStats.*;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.ee.BufferedServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet Filter that handles browser timeout events.
 * 
 * <p>
 * The init parameters for this filter are:
 * </p>
 * <ul>
 * <li><code>TimeoutPath</code>: the path to the timeout page.</li>
 * <li><code>LoginSessionAttribute</code>: the HTTP session attribute that
 * indicated a logged in user.</li>
 * </ul>
 * 
 * @author fcorneli
 * 
 */
public class ProfileFilter implements Filter {

	public static final String PROFILING_ENABLED = "ProfilingEnabled";

	private static final Log LOG = LogFactory.getLog(ProfileFilter.class);

	private boolean enabled;

	public void destroy() {

		LOG.debug("destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		/* Check to see if we're enabled and response type is compatible. */
		if (!enabled || !(response instanceof HttpServletResponse)) {
			LOG.debug("profile filter disabled");
			chain.doFilter(request, response);
			return;
		}

		/* Buffer the response so we can add our own headers. */
		LOG.debug("doFilter");
		BufferedServletResponseWrapper responseWrapper = new BufferedServletResponseWrapper(
				(HttpServletResponse) response);

		/* Build the response and time the duration. */
		LOG.debug("chain.doFilter");
		long startTime = System.currentTimeMillis();
		chain.doFilter(request, responseWrapper);
		long duration = System.currentTimeMillis() - startTime;

		/* Add our profiling results as HTTP headers. */
		addHeader(responseWrapper, REQUEST_TIME, duration);
		responseWrapper.commit();
	}

	private void addHeader(HttpServletResponse responseWrapper,
			ProfileStats header, long value) {

		responseWrapper.addHeader(header.getHeader(), String.valueOf(value));
	}

	public void init(FilterConfig config) throws ServletException {

		this.enabled = Boolean.parseBoolean(getInitParameter(config,
				PROFILING_ENABLED));

		LOG.debug("profiling enabled: " + this.enabled);
	}

	private String getInitParameter(FilterConfig config, String parameterName)
			throws UnavailableException {

		String value = config.getInitParameter(parameterName);
		if (null == value)
			throw new UnavailableException("missing init parameter: "
					+ parameterName);

		return value;
	}
}
