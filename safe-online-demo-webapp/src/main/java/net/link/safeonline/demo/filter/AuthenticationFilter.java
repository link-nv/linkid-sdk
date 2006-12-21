/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthenticationFilter implements Filter {

	private static final Log LOG = LogFactory
			.getLog(AuthenticationFilter.class);

	private String safeOnlineAuthenticationServiceUrl;

	private String applicationName;

	public void init(FilterConfig config) throws ServletException {
		LOG.debug("init");
		this.safeOnlineAuthenticationServiceUrl = config
				.getInitParameter("SafeOnlineAuthenticationServiceUrl");
		this.applicationName = config.getInitParameter("ApplicationName");
		LOG
				.debug("redirection url: "
						+ this.safeOnlineAuthenticationServiceUrl);
		LOG.debug("application name: " + this.applicationName);
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		LOG.debug("doFilter");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession session = httpRequest.getSession();
		String paramUsername = httpRequest.getParameter("username");
		if (null != paramUsername) {
			LOG.debug("doing a login via the SafeOnline username token");
			session.setAttribute("username", paramUsername);
		}
		String username = (String) session.getAttribute("username");
		if (null == username) {
			outputRedirectPage(httpRequest, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	private void outputRedirectPage(HttpServletRequest request,
			ServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		String redirectUrl = this.safeOnlineAuthenticationServiceUrl
				+ "?application="
				+ URLEncoder.encode(this.applicationName, "UTF-8")
				+ "&target="
				+ URLEncoder
						.encode(request.getRequestURL().toString(), "UTF-8");
		LOG.debug("redirect url: " + redirectUrl);
		out.println("<html>");
		out.println("<head>");
		out.println("<title>SafeOnline Demo</title>");
		out.println("<script language=\"JavaScript\">");
		out.println("<!--");
		out.println("redirectionTime = \"1000\";");
		out.println("redirectionUrl = \"" + redirectUrl + "\";");
		out.println("function redirectionTimer() {");
		out
				.println("self.setTimeout(\"self.location.href = redirectionUrl;\",redirectionTime);");
		out.println("}");
		out.println("// -->");
		out.println("</script>");
		out.println("</head>");
		out.println("<body onLoad=\"redirectionTimer()\">");
		out
				.println("<h1>Redirecting to the SafeOnline Authentication Service...</h1>");
		out.println("</body>");
		out.println("</html>");
	}

	public void destroy() {
		LOG.debug("destroy");
	}
}
