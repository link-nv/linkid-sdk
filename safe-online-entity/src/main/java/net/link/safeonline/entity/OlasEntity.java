/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.OlasEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.OlasEntity.QUERY_WHERE_CERTID;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.IndexColumn;

@Entity
@Table(name = "olas_entity")
@NamedQueries( {
		@NamedQuery(name = QUERY_LIST_ALL, query = "FROM OlasEntity o"),
		@NamedQuery(name = QUERY_WHERE_CERTID, query = "SELECT olas "
				+ "FROM OlasEntity AS olas "
				+ "WHERE olas.certificateIdentifier = :certificateIdentifier") })
public class OlasEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "olas.all";

	public static final String QUERY_WHERE_CERTID = "olas.certid";

	private String name;

	private String location;

	private byte[] encodedCert;

	private String certificateIdentifier;

	private transient X509Certificate certificate;

	public OlasEntity() {
		// empty
	}

	public OlasEntity(String name, String location, X509Certificate certificate) {
		this.name = name;
		this.location = location;
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

	/**
	 * Gives back the location of this Olas node
	 */
	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == (obj instanceof OlasEntity)) {
			return false;
		}
		OlasEntity rhs = (OlasEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).toString();
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_ALL)
		List<OlasEntity> listOlasEntities();
	}

	public static Query createQueryWhereCertificate(
			EntityManager entityManager, X509Certificate certificate) {
		byte[] encodedCertificate;
		try {
			encodedCertificate = certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new EJBException("Certificate encoding error: "
					+ e.getMessage(), e);
		}
		String certificateIdentifier = toCertificateIdentifier(encodedCertificate);
		Query query = entityManager.createNamedQuery(QUERY_WHERE_CERTID);
		query.setParameter("certificateIdentifier", certificateIdentifier);
		return query;
	}

}
