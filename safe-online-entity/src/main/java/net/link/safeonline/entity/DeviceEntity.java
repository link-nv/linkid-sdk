/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_WHERE_CLASS;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.IndexColumn;

@Entity
@Table(name = "devices")
@NamedQueries( {
		@NamedQuery(name = QUERY_LIST_ALL, query = "FROM DeviceEntity d"),
		@NamedQuery(name = QUERY_LIST_WHERE_CLASS, query = "SELECT d FROM DeviceEntity d "
				+ "WHERE d.deviceClass = :deviceClass") })
public class DeviceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "dev.all";

	public static final String QUERY_LIST_WHERE_CLASS = "dev.class";

	private String name;

	private DeviceClassEntity deviceClass;

	private String authenticationURL;

	private String registrationURL;

	private String removalURL;

	private byte[] encodedCert;

	private String certificateIdentifier;

	private transient X509Certificate certificate;

	private DeviceType deviceType;

	private List<AttributeTypeEntity> attributeTypes;

	private Map<String, DevicePropertyEntity> properties;

	private Map<String, DeviceDescriptionEntity> descriptions;

	public DeviceEntity() {
		// empty
	}

	public DeviceEntity(String name, DeviceType deviceType) {
		this.name = name;
		this.deviceType = deviceType;
	}

	public DeviceEntity(String name, DeviceClassEntity deviceClass,
			String authenticationURL, String registrationURL,
			String removalURL, X509Certificate certificate) {
		this.name = name;
		this.deviceClass = deviceClass;
		this.authenticationURL = authenticationURL;
		this.registrationURL = registrationURL;
		this.removalURL = removalURL;
		this.properties = new HashMap<String, DevicePropertyEntity>();
		this.descriptions = new HashMap<String, DeviceDescriptionEntity>();
		this.attributeTypes = new LinkedList<AttributeTypeEntity>();
		if (null != certificate) {
			try {
				this.encodedCert = certificate.getEncoded();
			} catch (CertificateEncodingException e) {
				throw new EJBException("certificate encoding error: "
						+ e.getMessage(), e);
			}
			this.certificateIdentifier = toCertificateIdentifier(this.encodedCert);
		}
	}

	@Id
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Enumerated(EnumType.STRING)
	public DeviceType getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "attribute_type")
	public List<AttributeTypeEntity> getAttributeTypes() {
		return this.attributeTypes;
	}

	public void setAttributeTypes(List<AttributeTypeEntity> attributeTypes) {
		this.attributeTypes = attributeTypes;
	}

	/**
	 * Gives back the device class.
	 * 
	 */
	@ManyToOne
	public DeviceClassEntity getDeviceClass() {
		return this.deviceClass;
	}

	public void setDeviceClass(DeviceClassEntity deviceClass) {
		this.deviceClass = deviceClass;
	}

	/**
	 * Retrieve the URL used when authentication through this device.
	 * 
	 */
	public String getAuthenticationURL() {
		return this.authenticationURL;
	}

	public void setAuthenticationURL(String authenticationURL) {
		this.authenticationURL = authenticationURL;
	}

	/**
	 * Retrieves the URL used when registering this device.
	 * 
	 */
	public String getRegistrationURL() {
		return this.registrationURL;
	}

	public void setRegistrationURL(String registrationURL) {
		this.registrationURL = registrationURL;
	}

	/**
	 * Retrieves the URL used when removing this device.
	 * 
	 */
	public String getRemovalURL() {
		return this.removalURL;
	}

	public void setRemovalURL(String removalURL) {
		this.removalURL = removalURL;
	}

	/**
	 * Gives back the encoded device certificate. Each device has a
	 * corresponding certificate. This certificate is used in the
	 * authentication/registration process.
	 * 
	 */
	@Lob
	@Column(length = 4 * 1024, nullable = true)
	public byte[] getEncodedCert() {
		return this.encodedCert;
	}

	/**
	 * Sets the encoded certificate data. Do not use this method directly. Use
	 * {@link #setCertificate(X509Certificate)} instead. This method should only
	 * be used by JPA.
	 * 
	 * @param encodedCert
	 */
	public void setEncodedCert(byte[] encodedCert) {
		this.encodedCert = encodedCert;
	}

	/**
	 * The certificate identifier is used during application authentication
	 * phase to associate a given certificate with it's corresponding
	 * application.
	 * 
	 */
	@Column(unique = true)
	@IndexColumn(name = "certID")
	public String getCertificateIdentifier() {
		return this.certificateIdentifier;
	}

	/**
	 * Sets the certificate identifier. Do not use this method directly. Use
	 * {@link #setCertificate(X509Certificate) setCertificate} instead. JPA
	 * requires this setter.
	 * 
	 * @param certificateIdentifier
	 * @see #setCertificate(X509Certificate)
	 */
	public void setCertificateIdentifier(String certificateIdentifier) {
		this.certificateIdentifier = certificateIdentifier;
	}

	@Transient
	public X509Certificate getCertificate() {
		if (null != this.certificate)
			return this.certificate;
		if (null == this.encodedCert)
			return null;
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X.509");
			InputStream inputStream = new ByteArrayInputStream(this.encodedCert);
			this.certificate = (X509Certificate) certificateFactory
					.generateCertificate(inputStream);
		} catch (CertificateException e) {
			throw new EJBException("cert factory error: " + e.getMessage());
		}
		return this.certificate;
	}

	/**
	 * Sets the X509 certificate of the application. Use this method to update
	 * the application certificate since this method keeps the certificate
	 * identifier in sync with the certificate.
	 * 
	 * @param certificate
	 */
	@Transient
	public void setCertificate(X509Certificate certificate) {
		byte[] encodedCertificate;
		try {
			encodedCertificate = certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new EJBException("certificate encoding error");
		}
		setEncodedCert(encodedCertificate);
		setCertificateIdentifier(toCertificateIdentifier(encodedCertificate));
	}

	/**
	 * Gives back the certificate identifier for a given encoded X509
	 * certificate.
	 * 
	 * @param encodedCertificate
	 */
	public static String toCertificateIdentifier(byte[] encodedCertificate) {
		String certificateIdentifier = DigestUtils.shaHex(encodedCertificate);
		return certificateIdentifier;
	}

	/**
	 * Returns map of device properties.
	 * 
	 */
	@OneToMany(mappedBy = "device")
	@MapKey(name = "name")
	public Map<String, DevicePropertyEntity> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, DevicePropertyEntity> properties) {
		this.properties = properties;
	}

	/**
	 * Returns map of i18n device descriptions.
	 * 
	 */
	@OneToMany(mappedBy = "device")
	@MapKey(name = "language")
	public Map<String, DeviceDescriptionEntity> getDescriptions() {
		return this.descriptions;
	}

	public void setDescriptions(
			Map<String, DeviceDescriptionEntity> descriptions) {
		this.descriptions = descriptions;
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_ALL)
		List<DeviceEntity> listDevices();

		@QueryMethod(QUERY_LIST_WHERE_CLASS)
		List<DeviceEntity> listDevices(@QueryParam("deviceClass")
		DeviceClassEntity deviceClass);
	}
}
