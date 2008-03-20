package net.link.safeonline.device.sdk.update.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.reg.saml2.Saml2BrowserPostHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExitServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(ExitServlet.class);

	private String updateUrl;

	public static final String UPDATE_URL_INIT_PARAM = "UpdateUrl";

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.updateUrl = getInitParameter(config, UPDATE_URL_INIT_PARAM);
	}

	private String getInitParameter(ServletConfig config, String initParamName)
			throws UnavailableException {
		String initParamValue = config.getServletContext().getInitParameter(
				initParamName);
		if (null == initParamValue)
			throw new UnavailableException("missing init parameter: "
					+ initParamName);
		return initParamValue;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Saml2BrowserPostHandler saml2BrowserPostHandler = Saml2BrowserPostHandler
				.getSaml2BrowserPostHandler(request);
		if (null == saml2BrowserPostHandler) {
			/*
			 * The landing page can only be used for finalizing an ongoing
			 * registration process. If no protocol handler is active then
			 * something must be going wrong here.
			 */
			String msg = "no protocol handler active";
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}
		List<String> ids = saml2BrowserPostHandler.handleResponse(request,
				response);
		if (ids.size() != 1 && ids.size() != 2) {
			String msg = "protocol handler could not finalize";
			LOG.error(msg);
			writeErrorPage(msg, response);
			return;
		}
		request.getSession().setAttribute("userId", ids.get(0));
		if (ids.size() == 2)
			request.getSession().setAttribute("registrationId", ids.get(1));

		response.sendRedirect(this.updateUrl);
	}

	private void writeErrorPage(String message, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		PrintWriter out = response.getWriter();
		out.println("<html>");
		{
			out.println("<head><title>Error</title></head>");
			out.println("<body>");
			{
				out.println("<h1>Error</h1>");
				out.println("<p>");
				{
					out.println(message);
				}
				out.println("</p>");
			}
			out.println("</body>");
		}
		out.println("</html>");
		out.close();
	}
}
