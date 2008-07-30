/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.OlasEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.OlasEntity.QUERY_WHERE_AUTHN_CERT_SUBJECT;
import static net.link.safeonline.entity.OlasEntity.QUERY_WHERE_SIGNING_CERT_SUBJECT;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

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
		@NamedQuery(name = QUERY_WHERE_AUTHN_CERT_SUBJECT, query = "SELECT olas "
				+ "FROM OlasEntity AS olas "
				+ "WHERE olas.authnCertificateSubject = :certificateSubject"),
		@NamedQuery(name = QUERY_WHERE_SIGNING_CERT_SUBJECT, query = "SELECT olas "
				+ "FROM OlasEntity AS olas "
				+ "WHERE olas.signingCertificateSubject = :certificateSubject") })
public class OlasEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "olas.all";

	public static final String QUERY_WHERE_AUTHN_CERT_SUBJECT = "olas.authn.cert.sub";

	public static final String QUERY_WHERE_SIGNING_CERT_SUBJECT = "olas.signing.cert.sub";

	private String name;

	private String protocol;

	private String hostname;

	private int port;

	private int sslPort;

	private String authnCertificateSubject;

	private String signingCertificateSubject;

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
			this.authnCertificateSubject = authnCertificate
					.getSubjectX500Principal().getName();
		}
		if (null != signingCertificate) {
			this.signingCertificateSubject = signingCertificate
					.getSubjectX500Principal().getName();
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

	@Column(unique = true)
	public String getAuthnCertificateSubject() {
		return this.authnCertificateSubject;
	}

	/**
	 * Sets the authentication certificate subject. Do not use this method
	 * directly. Use
	 * {@link #setAuthnCertificate(X509Certificate) setCertificate} instead. JPA
	 * requires this setter.
	 * 
	 * @param authnCertificateSubject
	 * @see #setAuthnCertificate(X509Certificate)
	 */
	public void setAuthnCertificateSubject(String authnCertificateSubject) {
		this.authnCertificateSubject = authnCertificateSubject;
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
		setAuthnCertificateSubject(authnCertificate.getSubjectX500Principal()
				.getName());
	}

	@Column(unique = true)
	public String getSigningCertificateSubject() {
		return this.signingCertificateSubject;
	}

	/**
	 * Sets the signing certificate subject. Do not use this method directly.
	 * Use {@link #setAuthnCertificate(X509Certificate) setCertificate} instead.
	 * JPA requires this setter.
	 * 
	 * @param signingCertificateSubject
	 * @see #setSigningCertificate(X509Certificate)
	 */
	public void setSigningCertificateSubject(String signingCertificateSubject) {
		this.signingCertificateSubject = signingCertificateSubject;
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
		setSigningCertificateSubject(signingCertificate
				.getSubjectX500Principal().getName());
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

		@QueryMethod(QUERY_WHERE_AUTHN_CERT_SUBJECT)
		List<OlasEntity> listOlasEntitiesWhereAuthnCertificateSubject(
				@QueryParam("certificateSubject") String certificateSubject);

		@QueryMethod(QUERY_WHERE_SIGNING_CERT_SUBJECT)
		List<OlasEntity> listOlasEntitiesWhereSigningCertificateSubject(
				@QueryParam("certificateSubject") String certificateSubject);
	}
}
