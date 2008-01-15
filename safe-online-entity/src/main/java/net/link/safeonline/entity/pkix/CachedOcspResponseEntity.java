/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.pkix;

import static net.link.safeonline.entity.pkix.CachedOcspResponseEntity.QUERY_DELETE_ALL;
import static net.link.safeonline.entity.pkix.CachedOcspResponseEntity.QUERY_DELETE_EXPIRED;
import static net.link.safeonline.entity.pkix.CachedOcspResponseEntity.QUERY_DELETE_PER_DOMAIN;
import static net.link.safeonline.entity.pkix.CachedOcspResponseEntity.QUERY_WHERE_KEY;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "cached_ocsp_responses")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_KEY, query = "SELECT cachedOcspResponse "
				+ "FROM CachedOcspResponseEntity AS CachedOcspResponse "
				+ "WHERE CachedOcspResponse.key = :key"),
		@NamedQuery(name = QUERY_DELETE_ALL, query = "DELETE FROM CachedOcspResponseEntity"),
		@NamedQuery(name = QUERY_DELETE_PER_DOMAIN, query = "DELETE "
				+ "FROM CachedOcspResponseEntity AS CachedOcspResponse "
				+ "WHERE CachedOcspResponse.trustDomain = :trustDomain"),
		@NamedQuery(name = QUERY_DELETE_EXPIRED, query = "DELETE "
				+ "FROM CachedOcspResponseEntity AS CachedOcspResponse "
				+ "WHERE CachedOcspResponse.entryDate < :expiryTime "
				+ "AND CachedOcspResponse.trustDomain = :trustDomain") })
public class CachedOcspResponseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_KEY = "cor.key";

	public static final String QUERY_DELETE_ALL = "cor.delall";

	public static final String QUERY_DELETE_PER_DOMAIN = "cor.deldomain";

	public static final String QUERY_DELETE_EXPIRED = "cor.expired";

	private static final int KEY_SIZE = 128;

	private long id;

	private String key;

	private boolean result;

	private TrustDomainEntity trustDomain;

	private Date entryDate;

	public CachedOcspResponseEntity() {
		// empty
	}

	public CachedOcspResponseEntity(String key, boolean result,
			TrustDomainEntity trustDomain) {
		this.key = key;
		this.result = result;
		this.trustDomain = trustDomain;
		this.entryDate = new Date(System.currentTimeMillis());
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(unique = true, nullable = false, length = KEY_SIZE, name = "kkey")
	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean getResult() {
		return this.result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	@ManyToOne
	public TrustDomainEntity getTrustDomain() {
		return this.trustDomain;
	}

	public void setTrustDomain(TrustDomainEntity trustDomain) {
		this.trustDomain = trustDomain;
	}

	public Date getEntryDate() {
		return this.entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("key",
				this.key).append("result", this.result).append("entry date",
				this.entryDate).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof CachedOcspResponseEntity) {
			return false;
		}
		CachedOcspResponseEntity rhs = (CachedOcspResponseEntity) obj;
		return new EqualsBuilder().append(this.key, rhs.key).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.key).toHashCode();
	}

	public interface QueryInterface {
		@QueryMethod(value = QUERY_WHERE_KEY, nullable = true)
		CachedOcspResponseEntity findCachedOcspResponse(@QueryParam("key")
		String key);

		@UpdateMethod(QUERY_DELETE_ALL)
		int deleteAll();

		@UpdateMethod(QUERY_DELETE_PER_DOMAIN)
		void deletePerDomain(@QueryParam("trustDomain")
		TrustDomainEntity trustDomain);

		@UpdateMethod(QUERY_DELETE_EXPIRED)
		void deleteExpired(@QueryParam("trustDomain")
		TrustDomainEntity trustDomain, @QueryParam("expiryTime")
		Date expiryTime);
	}
}
