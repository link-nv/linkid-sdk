/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.OlasEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.OlasEntity.QUERY_WHERE_AUTHN_CERTID;
import static net.link.safeonline.entity.OlasEntity.QUERY_WHERE_SIGNING_CERTID;

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

/**
 * This entity represents an OLAS node in the OLAS network.
 * 
 * An OLAS node has an authentication certificate used to authenticate against
 * other OLAS node when using its web services.
 * 
 * An OLAS node also has a signing certificate used for signing SAML tokens
 * issued from this node.
 * 
 * This entity is for example used by remote attributes to identify the location
 * of the actual attribute in the OLAS network.
 * 
 * @author wvdhaute
 * 
 */
@Entity
@Table(name = "olas_entity")
@NamedQueries( {
		@NamedQuery(name = QUERY_LIST_ALL, query = "FROM OlasEntity o"),
		@NamedQuery(name = QUERY_WHERE_AUTHN_CERTID, query = "SELECT olas "
				+ "FROM OlasEntity AS olas "
				+ "WHERE olas.authnCertificateIdentifier = :certificateIdentifier"),
		@NamedQuery(name = QUERY_WHERE_SIGNING_CERTID, query = "SELECT olas "
				+ "FROM OlasEntity AS olas "
				+ "WHERE olas.signingCertificateIdentifier = :certificateIdentifier") })
public class OlasEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "olas.all";

	public static final String QUERY_WHERE_AUTHN_CERTID = "olas.authn.certid";

	public static final String QUERY_WHERE_SIGNING_CERTID = "olas.signing.certid";

	private String name;

	private String protocol;

	private String hostname;

	private int port;

	private int sslPort;

	private byte[] encodedAuthnCert;

	private String authnCertificateIdentifier;

	private transient X509Certificate authnCertificate;

	private byte[] encodedSigningCert;

	private String signingCertificateIdentifier;

	private transient X509Certificate signingCertificate;

	public OlasEntity() {
		// empty
	}

	public OlasEntity(String name, String protocol, String hostname, int port,
			int sslPort, X509Certificate authnCertificate,
			X509Certificate signingCertificate) {
		this.name = name;
		this.protocol = protocol;
		this.hostname = hostname;
		this.port = port;
		this.sslPort = sslPort;
		if (null != authnCertificate) {
			try {
				this.encodedAuthnCert = authnCertificate.getEncoded();
			} catch (CertificateEncodingException e) {
				throw new EJBException("certificate encoding error: "
						+ e.getMessage(), e);
			}
			this.authnCertificateIdentifier = toCertificateIdentifier(this.encodedAuthnCert);
		}
		if (null != signingCertificate) {
			try {
				this.encodedSigningCert = signingCertificate.getEncoded();
			} catch (CertificateEncodingException e) {
				throw new EJBException("certificate encoding error: "
						+ e.getMessage(), e);
			}
			this.signingCertificateIdentifier = toCertificateIdentifier(this.encodedSigningCert);
		}
	}

	@Id
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocol() {

		return this.protocol;
	}

	public void setProtocol(String protocol) {

		this.protocol = protocol;
	}

	public String getHostname() {
		return this.hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {

		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getSslPort() {

		return this.sslPort;
	}

	public void setSslPort(int sslPort) {
		this.sslPort = sslPort;
	}

	/**
	 * Gives back the location of this OLAS node ( using default protocol )
	 */
	@Transient
	public String getLocation() {
		return String.format("%s://%s:%d", this.protocol, this.hostname,
				this.protocol.equals("http") ? this.port : this.sslPort);
	}

	/**
	 * Gives back the location of this OLAS node ( using HTTP protocol - no SSL )
	 */
	@Transient
	public String getHTTPLocation() {
		return String.format("http://%s:%d", this.hostname, this.port);
	}

	/**
	 * Gives back the location of this OLAS node ( using HTTPS protocol - SSL )
	 */
	@Transient
	public String getHTTPSLocation() {
		return String.format("https://%s:%d", this.hostname, this.sslPort);
	}

	/**
	 * Gives back the encoded node authentication certificate. This is used for
	 * OLAS nodes to authenticate against OLAS nodes when using its web
	 * services.
	 * 
	 */
	@Lob
	@Column(length = 4 * 1024, nullable = true)
	public byte[] getEncodedAuthnCert() {
		return this.encodedAuthnCert;
	}

	/**
	 * Sets the encoded authentication certificate data. Do not use this method
	 * directly. Use {@link #setAuthnCertificate(X509Certificate)} instead. This
	 * method should only be used by JPA.
	 * 
	 * @param encodedAuthnCert
	 */
	public void setEncodedAuthnCert(byte[] encodedAuthnCert) {
		this.encodedAuthnCert = encodedAuthnCert;
	}

	@Column(unique = true)
	@IndexColumn(name = "authnCertID")
	public String getAuthnCertificateIdentifier() {
		return this.authnCertificateIdentifier;
	}

	/**
	 * Sets the certificate identifier. Do not use this method directly. Use
	 * {@link #setAuthnCertificate(X509Certificate) setCertificate} instead. JPA
	 * requires this setter.
	 * 
	 * @param authnCertificateIdentifier
	 * @see #setAuthnCertificate(X509Certificate)
	 */
	public void setAuthnCertificateIdentifier(String authnCertificateIdentifier) {
		this.authnCertificateIdentifier = authnCertificateIdentifier;
	}

	@Transient
	public X509Certificate getAuthnCertificate() {
		if (null != this.authnCertificate)
			return this.authnCertificate;
		if (null == this.encodedAuthnCert)
			return null;
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X.509");
			InputStream inputStream = new ByteArrayInputStream(
					this.encodedAuthnCert);
			this.authnCertificate = (X509Certificate) certificateFactory
					.generateCertificate(inputStream);
		} catch (CertificateException e) {
			throw new EJBException("cert factory error: " + e.getMessage());
		}
		return this.authnCertificate;
	}

	/**
	 * Sets the X509 certificate of the application. Use this method to update
	 * the application certificate since this method keeps the certificate
	 * identifier in sync with the certificate.
	 * 
	 * @param authnCertificate
	 */
	@Transient
	public void setAuthnCertificate(X509Certificate authnCertificate) {
		byte[] encodedAuthnCertificate;
		try {
			encodedAuthnCertificate = authnCertificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new EJBException("certificate encoding error");
		}
		setEncodedAuthnCert(encodedAuthnCertificate);
		setAuthnCertificateIdentifier(toCertificateIdentifier(encodedAuthnCertificate));
	}

	/**
	 * Gives back the encoded node signing certificate. This is used to sign
	 * SAML tokens.
	 */
	@Lob
	@Column(length = 4 * 1024, nullable = true)
	public byte[] getEncodedSigningCert() {
		return this.encodedSigningCert;
	}

	/**
	 * Sets the encoded signing certificate data. Do not use this method
	 * directly. Use {@link #setAuthnCertificate(X509Certificate)} instead. This
	 * method should only be used by JPA.
	 * 
	 * @param encodedSigningCert
	 */
	public void setEncodedSigningCert(byte[] encodedSigningCert) {
		this.encodedSigningCert = encodedSigningCert;
	}

	@Column(unique = true)
	@IndexColumn(name = "signingCertID")
	public String getSigningCertificateIdentifier() {
		return this.signingCertificateIdentifier;
	}

	/**
	 * Sets the certificate identifier. Do not use this method directly. Use
	 * {@link #setAuthnCertificate(X509Certificate) setCertificate} instead. JPA
	 * requires this setter.
	 * 
	 * @param signingCertificateIdentifier
	 * @see #setSigningCertificate(X509Certificate)
	 */
	public void setSigningCertificateIdentifier(
			String signingCertificateIdentifier) {
		this.signingCertificateIdentifier = signingCertificateIdentifier;
	}

	@Transient
	public X509Certificate getSigningCertificate() {
		if (null != this.signingCertificate)
			return this.signingCertificate;
		if (null == this.encodedSigningCert)
			return null;
		try {
			CertificateFactory certificateFactory = CertificateFactory
					.getInstance("X.509");
			InputStream inputStream = new ByteArrayInputStream(
					this.encodedSigningCert);
			this.signingCertificate = (X509Certificate) certificateFactory
					.generateCertificate(inputStream);
		} catch (CertificateException e) {
			throw new EJBException("cert factory error: " + e.getMessage());
		}
		return this.signingCertificate;
	}

	/**
	 * Sets the X509 certificate of the application. Use this method to update
	 * the application certificate since this method keeps the certificate
	 * identifier in sync with the certificate.
	 * 
	 * @param signingCertificate
	 */
	@Transient
	public void setSigningCertificate(X509Certificate signingCertificate) {
		byte[] encodedSigningCertificate;
		try {
			encodedSigningCertificate = signingCertificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new EJBException("certificate encoding error");
		}
		setEncodedSigningCert(encodedSigningCertificate);
		setSigningCertificateIdentifier(toCertificateIdentifier(encodedSigningCertificate));
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
		if (this == obj)
			return true;
		if (null == obj)
			return false;
		if (false == obj instanceof OlasEntity)
			return false;
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

	public static Query createQueryWhereAuthnCertificate(
			EntityManager entityManager, X509Certificate authnCertificate) {
		byte[] encodedAuthnCertificate;
		try {
			encodedAuthnCertificate = authnCertificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new EJBException("Certificate encoding error: "
					+ e.getMessage(), e);
		}
		String certificateIdentifier = toCertificateIdentifier(encodedAuthnCertificate);
		Query query = entityManager.createNamedQuery(QUERY_WHERE_AUTHN_CERTID);
		query.setParameter("certificateIdentifier", certificateIdentifier);
		return query;
	}

	public static Query createQueryWhereSigningCertificate(
			EntityManager entityManager, X509Certificate signingCertificate) {
		byte[] encodedSigningCertificate;
		try {
			encodedSigningCertificate = signingCertificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new EJBException("Certificate encoding error: "
					+ e.getMessage(), e);
		}
		String certificateIdentifier = toCertificateIdentifier(encodedSigningCertificate);
		Query query = entityManager
				.createNamedQuery(QUERY_WHERE_SIGNING_CERTID);
		query.setParameter("certificateIdentifier", certificateIdentifier);
		return query;
	}
}
