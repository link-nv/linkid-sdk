/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.ejb.EJBException;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.TrustPointEntity.QUERY_WHERE_DOMAIN;

@Entity
@Table(name = "trust_point")
@NamedQueries(@NamedQuery(name = QUERY_WHERE_DOMAIN, query = "SELECT trustPoint "
		+ "FROM TrustPointEntity AS trustPoint "
		+ "WHERE trustPoint.trustDomain = :trustDomain"))
public class TrustPointEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_DOMAIN = "tp.domain";

	private TrustPointPK pk;

	private byte[] encodedCert;

	private X509Certificate certificate;

	private String issuerName;

	private TrustDomainEntity trustDomain;

	public TrustPointEntity() {
		// empty
	}

	public TrustPointEntity(TrustDomainEntity trustDomain,
			X509Certificate certificate) {
		this.trustDomain = trustDomain;
		this.certificate = certificate;
		this.issuerName = certificate.getIssuerX500Principal().toString();
		try {
			this.encodedCert = certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new EJBException("cert encoding error: " + e.getMessage());
		}
		String subjectName = certificate.getSubjectX500Principal().toString();
		this.pk = new TrustPointPK(trustDomain, subjectName);
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "domain", column = @Column(name = "domain")),
			@AttributeOverride(name = "subjectName", column = @Column(name = "subjectName")) })
	public TrustPointPK getPk() {
		return this.pk;
	}

	public void setPk(TrustPointPK pk) {
		this.pk = pk;
	}

	public String getIssuerName() {
		return this.issuerName;
	}

	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}

	@Lob
	@Column(length = 4 * 1024)
	public byte[] getEncodedCert() {
		return this.encodedCert;
	}

	public void setEncodedCert(byte[] encodedCert) {
		this.encodedCert = encodedCert;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "domain", insertable = false, updatable = false)
	public TrustDomainEntity getTrustDomain() {
		return trustDomain;
	}

	public void setTrustDomain(TrustDomainEntity trustDomain) {
		this.trustDomain = trustDomain;
	}

	@Transient
	public X509Certificate getCertificate() {
		if (null != this.certificate) {
			return certificate;
		}
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof TrustPointEntity) {
			return false;
		}
		TrustPointEntity rhs = (TrustPointEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("pk", this.pk).toString();
	}

	public static Query createQueryWhereDomain(EntityManager entityManager,
			TrustDomainEntity trustDomain) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_DOMAIN);
		query.setParameter("trustDomain", trustDomain);
		return query;
	}
}
