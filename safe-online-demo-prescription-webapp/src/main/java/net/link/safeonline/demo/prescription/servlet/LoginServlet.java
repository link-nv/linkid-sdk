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
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Login handling servlet. After SafeOnline performed its authentication it will
 * redirect to this servlet. This servlet will retrieve the 'admin' attribute.
 * Depending on the value of this attribute we redirect to a different page.
 * 
 * If the user has multiple roles active this servlet will redirect to a page
 * where the user can select the role under which he would like to operate.
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

		String wsLocation = config.getInitParameter("WsLocation");

		PrivateKeyEntry privateKeyEntry = DemoPrescriptionKeyStoreUtils
				.getPrivateKeyEntry();

		X509Certificate clientCertificate = (X509Certificate) privateKeyEntry
				.getCertificate();
		PrivateKey clientPrivateKey = privateKeyEntry.getPrivateKey();

		this.dataClient = new DataClientImpl(wsLocation, clientCertificate,
				clientPrivateKey);
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
		/*
		 * The "username" attribute has been set by the SafeOnline login filter.
		 */
		LOG.debug("username: " + username);

		boolean admin = getBoolean(username,
				DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME);
		boolean careProvider = getBoolean(username,
				DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME);
		boolean pharmacist = getBoolean(username,
				DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME);

		int rolesCount = 0;
		if (admin) {
			session.setAttribute("adminRole", "true");
			rolesCount++;
		}
		if (careProvider) {
			session.setAttribute("careProviderRole", "true");
			rolesCount++;
		}
		if (pharmacist) {
			session.setAttribute("pharmacistRole", "true");
			rolesCount++;
		}

		if (rolesCount == 0) {
			redirectToPatientPage(session, response);
			return;
		}

		if (rolesCount > 1) {
			/*
			 * In this case we let the user first pick the role under which he
			 * wants to operate.
			 */
			redirectToRolesPage(session, response);
			return;
		}

		if (admin) {
			redirectToAdminPage(session, response);
			return;
		}
		if (careProvider) {
			redirectToCareProviderPage(session, response);
			return;
		}
		if (pharmacist) {
			redirectToPharmacistPage(session, response);
			return;
		}
	}

	private boolean getBoolean(String username, String attributeName)
			throws ServletException {
		Attribute<Boolean> attribute;
		try {
			attribute = this.dataClient.getAttributeValue(username,
					attributeName, Boolean.class);
		} catch (RequestDeniedException e) {
			throw new ServletException(
					"count not retrieve prescription admin attribute");
		} catch (SubjectNotFoundException e) {
			throw new ServletException("subject not found");
		} catch (WSClientTransportException e) {
            throw new ServletException("connection failed");
        }

		if (null == attribute)
            return false;

		Boolean value = attribute.getValue();
		if (null == value)
            return false;

		return attribute.getValue();
	}

	private void redirectToPage(String page, String role, HttpSession session,
			HttpServletResponse response) throws IOException {
		session.setAttribute("role", role);
		response.sendRedirect(page);
	}

	private void redirectToCareProviderPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		redirectToPage("./care-provider.seam",
				PrescriptionConstants.CARE_PROVIDER_ROLE, session, response);
	}

	private void redirectToPharmacistPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		redirectToPage("./pharmacist.seam",
				PrescriptionConstants.PHARMACIST_ROLE, session, response);
	}

	private void redirectToPatientPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		redirectToPage("./patient.seam", PrescriptionConstants.PATIENT_ROLE,
				session, response);
	}

	private void redirectToRolesPage(@SuppressWarnings("unused")
	HttpSession session, HttpServletResponse response) throws IOException {
		response.sendRedirect("./roles.seam");
	}

	private void redirectToAdminPage(HttpSession session,
			HttpServletResponse response) throws IOException {
		redirectToPage("./admin.seam", PrescriptionConstants.ADMIN_ROLE,
				session, response);
	}
}
