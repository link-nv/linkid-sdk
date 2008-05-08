/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.resources;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.service.PublicApplicationService;
import net.link.safeonline.util.ee.EjbUtils;
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
public class ApplicationLogoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory
			.getLog(ApplicationLogoServlet.class);

	private PublicApplicationService publicApplicationService;

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		loadDependencies();
	}

	private void loadDependencies() {
		this.publicApplicationService = EjbUtils.getEJB(
				"SafeOnline/PublicApplicationServiceBean/local",
				PublicApplicationService.class);
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String applicationName = request.getParameter("applicationName");
		if (null == applicationName)
            throw new IllegalArgumentException("The application name must be provided.");

		try {
			PublicApplication application = this.publicApplicationService
					.getPublicApplication(applicationName);

			byte[] logo = application.getLogo();
			if (null == logo)
				return;

			MagicMatch magic = Magic.getMagicMatch(logo);
			if (!magic.getMimeType().startsWith("image/"))
				throw new IllegalStateException(
						"Not allowed to load non-image data out of the application URL field.");

			response.setContentType(magic.getMimeType());
			response.getOutputStream().write(logo);
			response.flushBuffer();
		}

		catch (ApplicationNotFoundException e) {
			LOG.error("Couldn't resolve application name: " + applicationName,
					e);
		} catch (MagicParseException e) {
		} catch (MagicMatchNotFoundException e) {
		} catch (MagicException e) {
		}
	}
}
