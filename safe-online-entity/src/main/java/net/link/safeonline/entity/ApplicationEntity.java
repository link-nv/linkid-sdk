/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_ALL;
import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_OWNER;

/**
 * Application Entity. We're not using the SecurityApplicationEntityListener
 * anymore for application ownership checking since this prevents the system
 * from initializing itself.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "application")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_ALL, query = "FROM ApplicationEntity"),
		@NamedQuery(name = QUERY_WHERE_OWNER, query = "SELECT application "
				+ "FROM ApplicationEntity AS application "
				+ "WHERE application.applicationOwner = :applicationOwner") })
// @EntityListeners(SecurityApplicationEntityListener.class)
public class ApplicationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_ALL = "app.all";

	public static final String QUERY_WHERE_OWNER = "app.owner";

	String name;

	String description;

	boolean allowUserSubscription;

	boolean removable;

	private ApplicationOwnerEntity applicationOwner;

	private byte[] encodedCert;

	private long currentApplicationIdentity;

	private transient X509Certificate certificate;

	public ApplicationEntity() {
		// empty
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner) {
		this(name, applicationOwner, true);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner, String description) {
		this(name, applicationOwner, description, true, true, null, 0);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner, String description,
			X509Certificate certificate) {
		this(name, applicationOwner, description, true, true, certificate, 0);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner,
			boolean allowUserSubscription) {
		this(name, applicationOwner, true, true);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner,
			boolean allowUserSubscription, boolean removable) {
		this(name, applicationOwner, null, allowUserSubscription, removable,
				null, 0);
	}

	public ApplicationEntity(String name,
			ApplicationOwnerEntity applicationOwner, String description,
			boolean allowUserSubscription, boolean removable,
			X509Certificate certificate, long identityVersion) {
		this.name = name;
		this.applicationOwner = applicationOwner;
		this.description = description;
		this.allowUserSubscription = allowUserSubscription;
		this.removable = removable;
		this.currentApplicationIdentity = identityVersion;
		if (null != certificate) {
			try {
				this.encodedCert = certificate.getEncoded();
			} catch (CertificateEncodingException e) {
				throw new EJBException("certificate encoding error: "
						+ e.getMessage(), e);
			}
		}
	}

	public ApplicationEntity(String applicationName,
			ApplicationOwnerEntity applicationOwner, String description,
			boolean allowUserSubscription, boolean removable) {
		this(applicationName, applicationOwner, description,
				allowUserSubscription, removable, null, 0);
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Id
	@Column(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAllowUserSubscription() {
		return this.allowUserSubscription;
	}

	public void setAllowUserSubscription(boolean allowUserSubscription) {
		this.allowUserSubscription = allowUserSubscription;
	}

	public boolean isRemovable() {
		return this.removable;
	}

	public void setRemovable(boolean removable) {
		this.removable = removable;
	}

	@ManyToOne(optional = false)
	public ApplicationOwnerEntity getApplicationOwner() {
		return this.applicationOwner;
	}

	public void setApplicationOwner(ApplicationOwnerEntity applicationOwner) {
		this.applicationOwner = applicationOwner;
	}

	@Lob
	@Column(length = 4 * 1024, nullable = true)
	public byte[] getEncodedCert() {
		return this.encodedCert;
	}

	public void setEncodedCert(byte[] encodedCert) {
		this.encodedCert = encodedCert;
	}

	public long getCurrentApplicationIdentity() {
		return this.currentApplicationIdentity;
	}

	public void setCurrentApplicationIdentity(long currentApplicationIdentity) {
		this.currentApplicationIdentity = currentApplicationIdentity;
	}

	@Transient
	public X509Certificate getCertificate() {
		if (null != this.certificate) {
			return certificate;
		}
		if (null == this.encodedCert) {
			return null;
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
		if (false == obj instanceof ApplicationEntity) {
			return false;
		}
		ApplicationEntity rhs = (ApplicationEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(
				"name", this.name).append("description", this.description)
				.append("allowUserSubscription", this.allowUserSubscription)
				.append("removable", this.removable).toString();
	}

	public static Query createQueryAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_ALL);
		return query;
	}

	public static Query createQueryWhereApplicationOwner(
			EntityManager entityManager, ApplicationOwnerEntity applicationOwner) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_OWNER);
		query.setParameter("applicationOwner", applicationOwner);
		return query;
	}
}
