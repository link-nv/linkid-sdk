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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.dao.TrustDomainDAO;
import net.link.safeonline.dao.TrustPointDAO;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.bean.AbstractInitBean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = Startable.JNDI_PREFIX + "BeIdStartableBean")
public class BeIdStartableBean extends AbstractInitBean {

	private static final Log LOG = LogFactory.getLog(BeIdStartableBean.class);

	public static final String INDEX_RESOURCE = "certs/beid/index.txt";

	@EJB
	private TrustDomainDAO trustDomainDAO;

	@EJB
	private TrustPointDAO trustPointDAO;

	public BeIdStartableBean() {
		List<AttributeTypeEntity> deviceAttributeTypeList = new ArrayList<AttributeTypeEntity>();

		AttributeTypeEntity givenNameAttributeType = new AttributeTypeEntity(
				BeIdConstants.GIVENNAME_ATTRIBUTE, "string", true, false);
		this.attributeTypes.add(givenNameAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				givenNameAttributeType, Locale.ENGLISH.getLanguage(),
				"Given name (BeID)", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				givenNameAttributeType, "nl", "Naam (BeID)", null));

		deviceAttributeTypeList.add(givenNameAttributeType);

		AttributeTypeEntity surnameAttributeType = new AttributeTypeEntity(
				BeIdConstants.SURNAME_ATTRIBUTE, "string", true, false);
		this.attributeTypes.add(surnameAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				surnameAttributeType, Locale.ENGLISH.getLanguage(),
				"Surname (BeID)", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				surnameAttributeType, "nl", "Achternaam (BeID)", null));

		deviceAttributeTypeList.add(surnameAttributeType);

		AttributeTypeEntity authenticationCertificateAttributeType = new AttributeTypeEntity(
				BeIdConstants.AUTH_CERT_ATTRIBUTE, "blob", true, false);
		this.attributeTypes.add(authenticationCertificateAttributeType);

		deviceAttributeTypeList.add(authenticationCertificateAttributeType);

		AttributeTypeEntity nrnAttributeType = new AttributeTypeEntity(
				BeIdConstants.NRN_ATTRIBUTE, "string", true, false);
		this.attributeTypes.add(nrnAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nrnAttributeType, Locale.ENGLISH.getLanguage(),
				"Identification number of the National Register", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nrnAttributeType, "nl",
				"Identificatienummer van het Rijksregister", null));

		deviceAttributeTypeList.add(nrnAttributeType);

		this.devices.put(BeIdConstants.BEID_DEVICE_ID, deviceAttributeTypeList);

	}

	public void postStart() {
		LOG.debug("post start");
		super.postStart();
		initTrustDomain();
	}

	@SuppressWarnings("unchecked")
	public void initTrustDomain() {
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
		return BeIdConstants.BEID_BOOT_PRIORITY;
	}
}
