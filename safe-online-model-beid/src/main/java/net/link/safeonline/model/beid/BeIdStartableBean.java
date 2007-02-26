/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid;

import java.io.IOException;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "BeIdStartableBean")
public class BeIdStartableBean implements Startable {

	private static final Log LOG = LogFactory.getLog(BeIdStartableBean.class);

	public static final String INDEX_RESOURCE = "certs/beid/index.txt";

	@EJB
	private TrustDomainDAO trustDomainDAO;

	@EJB
	private TrustPointDAO trustPointDAO;

	@EJB
	private AttributeTypeDAO attributeTypeDAO;

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

	@SuppressWarnings("unchecked")
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

		InputStream indexInputStream = classLoader
				.getResourceAsStream(INDEX_RESOURCE);
		List<String> lines;
		try {
			lines = IOUtils.readLines(indexInputStream);
		} catch (IOException e) {
			LOG.error("could not read the BeID certificate index file");
			return;
		}
		IOUtils.closeQuietly(indexInputStream);

		for (String certFilename : lines) {
			LOG.debug("loading " + certFilename);
			InputStream certInputStream = classLoader
					.getResourceAsStream("certs/beid/" + certFilename);
			X509Certificate certificate;
			try {
				certificate = (X509Certificate) certificateFactory
						.generateCertificate(certInputStream);
				this.trustPointDAO.addTrustPoint(beidTrustDomain, certificate);
			} catch (CertificateException e) {
				LOG.error("certificate error: " + e.getMessage(), e);
			}
		}
	}

	public void preStop() {
		LOG.debug("pre stop");
	}

	public int getPriority() {
		return Startable.PRIORITY_DONT_CARE;
	}
}
