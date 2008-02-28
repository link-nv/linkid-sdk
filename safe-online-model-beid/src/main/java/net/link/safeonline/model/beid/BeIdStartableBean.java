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
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.beid.keystore.BeidKeyStoreUtils;
import net.link.safeonline.entity.AttributeTypeDescriptionEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.model.bean.AbstractInitBean;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;

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
		AttributeTypeEntity givenNameAttributeType = new AttributeTypeEntity(
				BeIdConstants.GIVENNAME_ATTRIBUTE, DatatypeType.STRING, true,
				false);
		this.attributeTypes.add(givenNameAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				givenNameAttributeType, Locale.ENGLISH.getLanguage(),
				"Given name (BeID)", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				givenNameAttributeType, "nl", "Naam (BeID)", null));

		AttributeTypeEntity surnameAttributeType = new AttributeTypeEntity(
				BeIdConstants.SURNAME_ATTRIBUTE, DatatypeType.STRING, true,
				false);
		this.attributeTypes.add(surnameAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				surnameAttributeType, Locale.ENGLISH.getLanguage(),
				"Surname (BeID)", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				surnameAttributeType, "nl", "Achternaam (BeID)", null));

		AttributeTypeEntity nrnAttributeType = new AttributeTypeEntity(
				BeIdConstants.NRN_ATTRIBUTE, DatatypeType.STRING, true, false);
		this.attributeTypes.add(nrnAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nrnAttributeType, Locale.ENGLISH.getLanguage(),
				"Identification number of the National Register", null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				nrnAttributeType, "nl",
				"Identificatienummer van het Rijksregister", null));

		AttributeTypeEntity beidDeviceAttributeType = new AttributeTypeEntity(
				BeIdConstants.BEID_DEVICE_ATTRIBUTE, DatatypeType.COMPOUNDED,
				false, false);
		beidDeviceAttributeType.addMember(givenNameAttributeType, 0, true);
		beidDeviceAttributeType.addMember(surnameAttributeType, 1, true);
		beidDeviceAttributeType.addMember(nrnAttributeType, 2, true);
		this.attributeTypes.add(beidDeviceAttributeType);
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				beidDeviceAttributeType, Locale.ENGLISH.getLanguage(), "BeID",
				null));
		this.attributeTypeDescriptions.add(new AttributeTypeDescriptionEntity(
				beidDeviceAttributeType, "nl", "BeID", null));

		X509Certificate certificate = (X509Certificate) BeidKeyStoreUtils
				.getPrivateKeyEntry().getCertificate();
		this.trustedCertificates.put(certificate,
				SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN);

		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		Properties props = new Properties();
		try {
			props.load(classLoader
					.getResourceAsStream("properties/beid/beid.properties"));
		} catch (Exception e) {
			throw new EJBException("Could not open beid properties");
		}

		String hostname = props.getProperty("hostname");
		String port = props.getProperty("port");

		this.devices.add(new Device(SafeOnlineConstants.BEID_DEVICE_ID,
				SafeOnlineConstants.PKI_DEVICE_CLASS, "https://" + hostname
						+ ":" + port + "/olas-beid/auth", "https://" + hostname
						+ ":" + port + "/olas-beid/reg",
				"beid/new-user-beid.seam", "https://" + hostname + ":" + port
						+ "/olas-beid/remove", null, certificate,
				beidDeviceAttributeType));
		this.deviceDescriptions.add(new DeviceDescription(
				SafeOnlineConstants.BEID_DEVICE_ID, "nl", "Belgische eID"));
		this.deviceDescriptions.add(new DeviceDescription(
				SafeOnlineConstants.BEID_DEVICE_ID, Locale.ENGLISH
						.getLanguage(), "Belgian eID"));

	}

	@Override
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

	@Override
	public void preStop() {
		LOG.debug("pre stop");
	}

	@Override
	public int getPriority() {
		return BeIdConstants.BEID_BOOT_PRIORITY;
	}
}
