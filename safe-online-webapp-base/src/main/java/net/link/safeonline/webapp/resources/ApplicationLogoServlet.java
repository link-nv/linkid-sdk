/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.sdk.servlet.AbstractInjectionServlet;
import net.link.safeonline.service.PublicApplicationService;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h2>{@link ApplicationLogoServlet} - [in short] (TODO).</h2>
 * <p>
 * [description / usage].
 * </p>
 * <p>
 * <i>Dec 6, 2007</i>
 * </p>
 * 
 * @author mbillemo
 */
public class ApplicationLogoServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(ApplicationLogoServlet.class);
	private static final String SPACER = "spacer.gif";

	@EJB(mappedName = PublicApplicationService.JNDI_BINDING)
	private PublicApplicationService publicApplicationService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void invokeGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		boolean logoWritten = false;
		String applicationName = request.getParameter("applicationName");
		if (null == applicationName) {
			throw new IllegalArgumentException(
					"The application name must be provided.");
		}

		try {
			PublicApplication application = this.publicApplicationService
					.findPublicApplication(applicationName);
			if (application == null)
				return;

			byte[] logo = application.getLogo();
			if (null == logo)
				return;

			MagicMatch magic = Magic.getMagicMatch(logo);
			if (!magic.getMimeType().startsWith("image/")) {
				throw new IllegalStateException(
						"Not allowed to load non-image data out of the application URL field.");
			}

			response.setContentType(magic.getMimeType());
			response.getOutputStream().write(logo);
			logoWritten = true;
		}

		catch (MagicParseException e) {
		} catch (MagicMatchNotFoundException e) {
		} catch (MagicException e) {
		}

		finally {
			if (!logoWritten) {
				response.setContentType("image/gif");

				int read;
				byte[] buf = new byte[42];
				InputStream spacerUrl = ClassLoader
						.getSystemResourceAsStream(SPACER);
				if (spacerUrl == null) {
					LOG.warn("Spacer not found!");
				} else {
					while ((read = spacerUrl.read(buf)) > -1) {
						response.getOutputStream().write(buf, 0, read);
					}
				}
			}

			response.flushBuffer();
		}
	}
}
