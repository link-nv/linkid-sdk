/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.security.Security;
import java.util.Set;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import net.link.safeonline.audit.AuditContextPolicyContextHandler;
import net.link.safeonline.util.jacc.ProfilingPolicyContextHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Service that prepares the runtime for the SafeOnline application.
 * 
 * @author fcorneli
 * 
 */
public class RuntimeService extends ServiceMBeanSupport implements
		RuntimeServiceMBean {

	private static final Log LOG = LogFactory.getLog(RuntimeService.class);

	private boolean manageBouncyCastleProvider;

	@Override
	protected void startService() throws Exception {
		super.startService();
		LOG.debug("start");
		registerBouncyCastle();
		registerAuditContextPolicyContextHandler();
		registerProfilingPolicyContextHandler();
	}

	@Override
	protected void stopService() throws Exception {
		LOG.debug("stop");
		unregisterBouncyCastle();
		super.stopService();
	}

	private void registerBouncyCastle() {
		if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
			LOG.debug("Installing BouncyCastle security provider...");
			Security.addProvider(new BouncyCastleProvider());
			this.manageBouncyCastleProvider = true;
		}
	}

	private void unregisterBouncyCastle() {
		if (this.manageBouncyCastleProvider) {
			LOG.debug("Removing BouncyCastle security provider...");
			Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
			this.manageBouncyCastleProvider = false;
		}
	}

	@SuppressWarnings("unchecked")
	private void registerAuditContextPolicyContextHandler()
			throws PolicyContextException {
		Set<String> handlerKeys = PolicyContext.getHandlerKeys();
		if (false == handlerKeys
				.contains(AuditContextPolicyContextHandler.AUDIT_CONTEXT_KEY)) {
			LOG.debug("Registering audit context policy context handler...");
			AuditContextPolicyContextHandler auditContextPolicyContextHandler = new AuditContextPolicyContextHandler();
			PolicyContext.registerHandler(
					AuditContextPolicyContextHandler.AUDIT_CONTEXT_KEY,
					auditContextPolicyContextHandler, false);
		}
	}

	private void registerProfilingPolicyContextHandler()
			throws PolicyContextException {
		@SuppressWarnings("unchecked")
		Set<String> handlerKeys = PolicyContext.getHandlerKeys();
		if (false == handlerKeys
				.contains(ProfilingPolicyContextHandler.PROFILING_CONTEXT_KEY)) {
			LOG.debug("Registering profiling policy context handler...");
			ProfilingPolicyContextHandler profilingPolicyContextHandler = new ProfilingPolicyContextHandler();
			PolicyContext.registerHandler(
					ProfilingPolicyContextHandler.PROFILING_CONTEXT_KEY,
					profilingPolicyContextHandler, false);
		}
	}
}
