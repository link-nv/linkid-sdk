/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.servlet;

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

import net.link.safeonline.demo.payment.PaymentConstants;
import net.link.safeonline.demo.payment.keystore.DemoPaymentKeyStoreUtils;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(LoginServlet.class);

	private DataClient dataClient;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		LOG.debug("init");

		String wsHostName = config.getInitParameter("WsHostName");
		String wsHostPort = config.getInitParameter("WsHostPort");

		PrivateKeyEntry privateKeyEntry = DemoPaymentKeyStoreUtils
				.getPrivateKeyEntry();

		X509Certificate clientCertificate = (X509Certificate) privateKeyEntry
				.getCertificate();
		PrivateKey clientPrivateKey = privateKeyEntry.getPrivateKey();

		this.dataClient = new DataClientImpl(wsHostName + ":" + wsHostPort,
				clientCertificate, clientPrivateKey);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Since the SAML protocol can enter the application via an HTTP POST we
		 * also need to implement the doPost method.
		 */
		doGet(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		String username = (String) session.getAttribute("username");
		LOG.debug("username: " + username);

		Attribute<Boolean> paymentAdminAttribute;
		try {
			paymentAdminAttribute = this.dataClient.getAttributeValue(username,
					DemoConstants.PAYMENT_ADMIN_ATTRIBUTE_NAME, Boolean.class);
		} catch (RequestDeniedException e) {
			throw new ServletException(
					"count not retrieve payment admin attribute");
		} catch (SubjectNotFoundException e) {
			throw new ServletException("subject not found");
		}

		if (null == paymentAdminAttribute) {
			redirectToOverviewPage(session, response);
			return;
		}

		Boolean value = paymentAdminAttribute.getValue();
		if (null == value) {
			redirectToOverviewPage(session, response);
			return;
		}

		if (false == value) {
			redirectToOverviewPage(session, response);
			return;
		}

		redirectToAdminPage(session, response);
	}

	private void redirectToOverviewPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", PaymentConstants.USER_ROLE);
		/*
		 * The role attribute is used by the LawyerLoginModule for
		 * authorization.
		 */
		response.sendRedirect("./overview.seam");
	}

	private void redirectToAdminPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", PaymentConstants.ADMIN_ROLE);
		response.sendRedirect("./search.seam");
	}

}
