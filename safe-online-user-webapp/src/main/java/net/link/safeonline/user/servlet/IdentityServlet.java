/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.user.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.TrustDomainNotFoundException;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The identity servlet implementation. This servlet receives its data from the
 * BeID via the IdentityApplet.
 * 
 * @author fcorneli
 * 
 */
public class IdentityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(IdentityServlet.class);

	private CredentialService credentialService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		loadCredentialService();
	}

	private void loadCredentialService() {
		this.credentialService = EjbUtils.getEJB(
				"SafeOnline/CredentialServiceBean/local",
				CredentialService.class);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("doPost");
		String contentType = request.getContentType();
		LOG.debug("content type: " + contentType);
		if (false == "application/octet-stream".equals(contentType)) {
			LOG.error("content-type should be application/octet-stream");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		InputStream contentInputStream = request.getInputStream();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copy(contentInputStream, outputStream);
		byte[] identityStatementData = outputStream.toByteArray();

		PrintWriter writer = response.getWriter();
		try {
			this.credentialService
					.mergeIdentityStatement(identityStatementData);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (TrustDomainNotFoundException e) {
			LOG.error("trust domain not found: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			writer.println("trust domain not found");
		} catch (PermissionDeniedException e) {
			LOG.error("permission denied: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			writer.println("permission denied");
		} catch (ArgumentIntegrityException e) {
			LOG.error("integrity error: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			writer.println("integrity check failed");
		} catch (Exception e) {
			LOG.error("credential service error: " + e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			writer.println("internal error");
		}
	}
}
