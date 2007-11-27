/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.performance;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;

import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "PerformanceStartableBean")
public class PerformanceStartableBean extends AbstractInitBean {

	public static final String PERFORMANCE_APPLICATION_NAME = "performance-application";

	public PerformanceStartableBean() {
		PrivateKeyEntry perfPrivateKeyEntry = PerformanceKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate perfCertificate = (X509Certificate) perfPrivateKeyEntry
				.getCertificate();

		this.registeredApplications.add(new Application(
				PERFORMANCE_APPLICATION_NAME, "owner", null, null, null, null,
				true, false, perfCertificate, true, IdScopeType.USER));
		this.trustedCertificates.add(perfCertificate);
	}

	@Override
	public int getPriority() {
		return Startable.PRIORITY_BOOTSTRAP - 1;
	}
}
