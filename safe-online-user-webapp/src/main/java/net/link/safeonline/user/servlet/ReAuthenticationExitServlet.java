package net.link.safeonline.user.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.service.ReAuthenticationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This HTTP servlet cleans up the stateful ReAuthentication service bean used
 * by the user web application.
 * 
 * @author wvdhaute
 * 
 */
public class ReAuthenticationExitServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String RE_AUTH_SERVICE_ATTRIBUTE = "reAuthenticationService";

	private static final Log LOG = LogFactory
			.getLog(ReAuthenticationExitServlet.class);

	public static final String EXIT_URL_INIT_PARAM = "ExitUrl";

	private String exitUrl;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.exitUrl = getInitParameter(config, EXIT_URL_INIT_PARAM);
	}

	public String getInitParameter(ServletConfig config,
			String initParameterName) throws UnavailableException {
		String paramValue = config.getInitParameter(initParameterName);
		if (null == paramValue) {
			throw new UnavailableException("missing init parameter: "
					+ initParameterName);
		}
		return paramValue;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOG.debug("GET entry");
		handleInvocation(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		LOG.debug("POST entry");
		handleInvocation(request, response);
	}

	private void handleInvocation(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		ReAuthenticationService reAuthenticationService = (ReAuthenticationService) session
				.getAttribute(RE_AUTH_SERVICE_ATTRIBUTE);
		if (null != reAuthenticationService) {
			/*
			 * Something unusual has happened. Do a nice cleanup and create a
			 * new one.
			 */
			LOG.debug("aborting re-authentication service instance");
			reAuthenticationService.abort();
		}
		session.removeAttribute(RE_AUTH_SERVICE_ATTRIBUTE);
		response.sendRedirect(this.exitUrl);
	}

}
