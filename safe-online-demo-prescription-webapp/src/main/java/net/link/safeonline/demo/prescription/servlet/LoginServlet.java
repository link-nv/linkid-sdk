/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.servlet;

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

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.keystore.DemoPrescriptionKeyStoreUtils;
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
 * redirect to this servlet. This servlet will retrieve the 'admin' attribute.
 * Depending on the value of this attribute we redirect to a different page.
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

		PrivateKeyEntry privateKeyEntry = DemoPrescriptionKeyStoreUtils
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
		/*
		 * The "username" attribute has been set by the SafeOnline login filter.
		 */
		LOG.debug("username: " + username);

		DataValue adminAttribute;
		DataValue careProviderAttribute;
		DataValue pharmacistAttribute;
		try {
			adminAttribute = this.dataClient.getAttributeValue(username,
					DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME);
			careProviderAttribute = this.dataClient.getAttributeValue(username,
					DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME);
			pharmacistAttribute = this.dataClient.getAttributeValue(username,
					DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME);
		} catch (RequestDeniedException e) {
			throw new ServletException(
					"count not retrieve prescription admin attribute");
		} catch (SubjectNotFoundException e) {
			throw new ServletException("subject not found");
		}

		if (null != adminAttribute) {
			String adminValue = adminAttribute.getValue();
			if (Boolean.valueOf(adminValue)) {
				redirectToAdminPage(session, response);
				return;
			}
		}

		if (null != careProviderAttribute) {
			String careProviderValue = careProviderAttribute.getValue();
			if (Boolean.valueOf(careProviderValue)) {
				redirectToCareProviderPage(session, response);
				return;
			}
		}

		if (null != pharmacistAttribute) {
			String pharmacistValue = pharmacistAttribute.getValue();
			if (Boolean.valueOf(pharmacistValue)) {
				redirectToPharmacistPage(session, response);
				return;
			}
		}

		redirectToPatientPage(session, response);
	}

	private void redirectToCareProviderPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", PrescriptionConstants.CARE_PROVIDER_ROLE);
		response.sendRedirect("./care-provider.seam");
	}

	private void redirectToPharmacistPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", PrescriptionConstants.PHARMACIST_ROLE);
		response.sendRedirect("./pharmacist.seam");
	}

	private void redirectToPatientPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", PrescriptionConstants.PATIENT_ROLE);
		response.sendRedirect("./patient.seam");
	}

	private void redirectToAdminPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", PrescriptionConstants.ADMIN_ROLE);
		response.sendRedirect("./admin.seam");
	}
}
