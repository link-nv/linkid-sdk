/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.TrustDomainEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "BeIdStartableBean")
public class BeIdStartableBean implements Startable {

	private static final Log LOG = LogFactory.getLog(BeIdStartableBean.class);

	@EJB
	private TrustDomainDAO trustDomainDAO;

	@EJB
	private TrustPointDAO trustPointDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

	private static final String[] certResources = {
			"certs/beid/belgiumrca.crt", "certs/beid/citizen200601.crt",
			"certs/beid/citizen200602.crt", "certs/beid/citizen200603.crt",
			"certs/beid/citizen200604.crt", "certs/beid/citizen200605.crt",
			"certs/beid/citizen200606.crt", "certs/beid/citizen200607.crt",
			"certs/beid/citizen200608.crt", "certs/beid/citizen200609.crt",
			"certs/beid/citizen200610.crt", "certs/beid/citizen200611.crt",
			"certs/beid/citizen200612.crt", "certs/beid/citizen200613.crt",
			"certs/beid/citizen200614.crt", "certs/beid/citizen200615.crt",
			"certs/beid/citizen200616.crt", "certs/beid/citizen200617.crt",
			"certs/beid/citizen200618.crt", "certs/beid/citizen200619.crt",
			"certs/beid/citizen200620.crt" };

	private static List<AttributeTypeEntity> attributeTypes;

	static {
		BeIdStartableBean.attributeTypes = new LinkedList<AttributeTypeEntity>();
		attributeTypes.add(new AttributeTypeEntity(
				BeIdConstants.GIVENNAME_ATTRIBUTE, "string", true, false));
		attributeTypes.add(new AttributeTypeEntity(
				BeIdConstants.SURNAME_ATTRIBUTE, "string", true, false));
		attributeTypes.add(new AttributeTypeEntity(
				BeIdConstants.AUTH_CERT_ATTRIBUTE, "blob", true, false));

	}

	public void postStart() {
		LOG.debug("post start");
		initTrustDomain();
		initAttributeTypes();
	}

	private void initAttributeTypes() {
		for (AttributeTypeEntity attributeType : BeIdStartableBean.attributeTypes) {
			AttributeTypeEntity existingAttributeType = this.attributeTypeDAO
					.findAttributeType(attributeType.getName());
			if (null != existingAttributeType) {
				continue;
			}
			this.attributeTypeDAO.addAttributeType(attributeType);
		}
	}

	private void initTrustDomain() {
		TrustDomainEntity beidTrustDomain = this.trustDomainDAO
				.findTrustDomain(BeIdPkiProvider.TRUST_DOMAIN_NAME);
		if (null != beidTrustDomain) {
			return;
		}

		beidTrustDomain = this.trustDomainDAO.addTrustDomain(
				BeIdPkiProvider.TRUST_DOMAIN_NAME, true);

		CertificateFactory certificateFactory;
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOG.error("could not get cert factory: " + e.getMessage(), e);
			return;
		}

		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		for (String certResource : certResources) {
			LOG.debug("loading cert resource: " + certResource);
			InputStream certInputStream = classLoader
					.getResourceAsStream(certResource);
			X509Certificate certificate;
			try {
				certificate = (X509Certificate) certificateFactory
						.generateCertificate(certInputStream);
			} catch (CertificateException e) {
				LOG.error("certificate error: " + e.getMessage(), e);
				continue;
			}
			this.trustPointDAO.addTrustPoint(beidTrustDomain, certificate);
		}
	}

	public void preStop() {
		LOG.debug("pre stop");
	}
}
