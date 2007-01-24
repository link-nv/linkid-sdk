/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.security.Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.system.ServiceMBeanSupport;

public class RuntimeService extends ServiceMBeanSupport implements
		RuntimeServiceMBean {

	private static final Log LOG = LogFactory.getLog(RuntimeService.class);

	private boolean manageBouncyCastleProvider;

	@Override
	protected void startService() throws Exception {
		super.startService();
		LOG.debug("start");
		if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
			LOG.debug("Installing BouncyCastle security provider...");
			Security.addProvider(new BouncyCastleProvider());
			this.manageBouncyCastleProvider = true;
		}
	}

	@Override
	protected void stopService() throws Exception {
		LOG.debug("stop");
		if (this.manageBouncyCastleProvider) {
			LOG.debug("Removing BouncyCastle security provider...");
			Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
			this.manageBouncyCastleProvider = false;
		}
		super.stopService();
	}
}
