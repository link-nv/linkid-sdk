/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer.servlet;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.demo.lawyer.LawyerConstants;
import net.link.safeonline.demo.lawyer.keystore.DemoLawyerKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.data.DataValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Login handling servlet. After SafeOnline performed its authentication it will
 * redirect to this servlet. This servlet will retrieve the 'bar admin'
 * attribute. Depending on the value of this attribute we redirect to a
 * different page.
 * 
 * @author fcorneli
 * 
 */
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(LoginServlet.class);

	private DataClient dataClient;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		LOG.debug("init");

		String location = config.getInitParameter("LocalHostName");

		PrivateKeyEntry privateKeyEntry = DemoLawyerKeyStoreUtils
				.getPrivateKeyEntry();

		X509Certificate clientCertificate = (X509Certificate) privateKeyEntry
				.getCertificate();
		PrivateKey clientPrivateKey = privateKeyEntry.getPrivateKey();

		this.dataClient = new DataClientImpl(location, clientCertificate,
				clientPrivateKey);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		String username = (String) session.getAttribute("username");
		LOG.debug("username: " + username);

		DataValue barAdminAttribute;
		try {
			barAdminAttribute = this.dataClient.getAttributeValue(username,
					DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME);
		} catch (RequestDeniedException e) {
			throw new ServletException("count not retrieve baradmin attribute");
		} catch (SubjectNotFoundException e) {
			throw new ServletException("subject not found");
		}

		if (null == barAdminAttribute) {
			redirectToStatusPage(session, response);
			return;
		}

		String value = barAdminAttribute.getValue();
		if (null == value) {
			redirectToStatusPage(session, response);
			return;
		}

		if (false == "true".equals(value)) {
			redirectToStatusPage(session, response);
			return;
		}

		redirectToOverviewPage(session, response);
	}

	private void redirectToStatusPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", LawyerConstants.USER_ROLE);
		/*
		 * The role attribute is used by the LawyerLoginModule for
		 * authorization.
		 */
		response.sendRedirect("./status.seam");
	}

	private void redirectToOverviewPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", LawyerConstants.ADMIN_ROLE);
		response.sendRedirect("./overview.seam");
	}
}
