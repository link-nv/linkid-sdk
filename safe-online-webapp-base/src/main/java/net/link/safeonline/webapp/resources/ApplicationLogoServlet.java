/*
 *   Copyright 2007, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.link.safeonline.webapp.resources;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.entity.ApplicationEntity;
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

	private ApplicationService applicationService;

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		loadDependencies();
	}

	private void loadDependencies() {
		this.applicationService = EjbUtils.getEJB(
				"SafeOnline/ApplicationServiceBean/local",
				ApplicationService.class);
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String applicationName = request.getParameter("applicationName");
		try {
			ApplicationEntity application = this.applicationService
					.getApplication(applicationName);

			byte[] logo = application.getApplicationLogo();
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
