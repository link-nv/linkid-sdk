/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.performance;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@LocalBinding(jndiBinding = PerformanceService.JNDI_BINDING_NAME)
public class PerformanceServiceBean implements PerformanceService {

	private static final Log LOG = LogFactory
			.getLog(PerformanceServiceBean.class);

	private PrivateKey privateKey;

	private X509Certificate certificate;

	public X509Certificate getCertificate() {
		LOG.debug("get certificate");
		return this.certificate;
	}

	public PrivateKey getPrivateKey() {
		LOG.debug("get private key");
		return this.privateKey;
	}

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("post construct callback");
		PrivateKeyEntry perfPrivateKeyEntry = PerformanceKeyStoreUtils
				.getPrivateKeyEntry();
		this.privateKey = perfPrivateKeyEntry.getPrivateKey();
		this.certificate = (X509Certificate) perfPrivateKeyEntry
				.getCertificate();
	}
}
