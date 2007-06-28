/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.link.safeonline.demo.mandate.AuthorizationService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MandateBootstrapListener implements ServletContextListener {

	private static final Log LOG = LogFactory
			.getLog(MandateBootstrapListener.class);

	private AuthorizationService authorizationService;

	public void contextInitialized(ServletContextEvent event) {
		LOG.debug("context initialized");

		this.authorizationService = EjbUtils.getEJB(
				AuthorizationService.JNDI_BINDING, AuthorizationService.class);
		this.authorizationService.bootstrap();
	}

	public void contextDestroyed(ServletContextEvent event) {
		LOG.debug("context destroyed");
	}
}
