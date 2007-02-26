/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.demo;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.demo.keystore.DemoKeyStoreUtils;
import net.link.safeonline.entity.TrustDomainEntity;
import net.link.safeonline.entity.TrustPointEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "DemoStartableBean")
public class DemoStartableBean implements Startable {

	private static final Log LOG = LogFactory.getLog(DemoStartableBean.class);

	@EJB
	private TrustDomainDAO trustDomainDAO;

	@EJB
	private TrustPointDAO trustPointDAO;

	public int getPriority() {
		return Startable.PRIORITY_DONT_CARE;
	}

	public void postStart() {
		LOG.debug("postStart");
		addDemoCertificateAsTrustPoint();
		// TODO: move demo related init from systemInitialization to here
	}

	public void preStop() {
		LOG.debug("preStop");
	}

	private void addDemoCertificateAsTrustPoint() {
		PrivateKeyEntry privateKeyEntry = DemoKeyStoreUtils
				.getPrivateKeyEntry();
		X509Certificate certificate = (X509Certificate) privateKeyEntry
				.getCertificate();

		TrustDomainEntity applicationTrustDomain = this.trustDomainDAO
				.findTrustDomain(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN);
		if (null == applicationTrustDomain) {
			LOG.fatal("application trust domain not found");
			return;
		}

		TrustPointEntity demoTrustPoint = this.trustPointDAO.findTrustPoint(
				applicationTrustDomain, certificate);
		if (null != demoTrustPoint) {
			return;
		}

		this.trustPointDAO.addTrustPoint(applicationTrustDomain, certificate);
	}
}
