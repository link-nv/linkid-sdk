/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.UsageAgreementEntity.QUERY_WHERE_APPLICATION;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT usageAgreement "
		+ "FROM UsageAgreementEntity AS usageAgreement "
		+ "WHERE usageAgreement.application = :application "
		+ "ORDER BY usageAgreement.pk.usageAgreementVersion DESC") })
public class UsageAgreementEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_APPLICATION = "ua.app";

	public static final String APPLICATION_COLUMN_NAME = "application";

	public static final String USAGE_AGREEMENT_VERSION_COLUMN_NAME = "usageAgreementVersion";

	private UsageAgreementPK pk;

	private ApplicationEntity application;

	private Set<UsageAgreementTextEntity> usageAgreementTexts;

	public UsageAgreementEntity() {
		this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
	}

	public UsageAgreementEntity(ApplicationEntity application,
			Long usageAgreementVersion) {
		this.pk = new UsageAgreementPK(application.getName(),
				usageAgreementVersion);
		this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "application", column = @Column(name = APPLICATION_COLUMN_NAME)),
			@AttributeOverride(name = "usageAgreementVersion", column = @Column(name = USAGE_AGREEMENT_VERSION_COLUMN_NAME)) })
	public UsageAgreementPK getPk() {
		return this.pk;
	}

	public void setPk(UsageAgreementPK pk) {
		this.pk = pk;
	}

	@OneToMany(fetch = FetchType.EAGER)
	public Set<UsageAgreementTextEntity> getUsageAgreementTexts() {
		return usageAgreementTexts;
	}

	public void setUsageAgreementTexts(
			Set<UsageAgreementTextEntity> usageAgreementTexts) {
		this.usageAgreementTexts = usageAgreementTexts;
	}

	@Transient
	public Long getUsageAgreementVersion() {
		return this.pk.getUsageAgreementVersion();
	}

	@Transient
	public void releaseNewVersion(Long usageAgreementVersion) {
		this.pk.setUsageAgreementVersion(usageAgreementVersion);
		for (UsageAgreementTextEntity usageAgreementText : this.usageAgreementTexts) {
			usageAgreementText.getPk().setUsageAgreementVersion(
					usageAgreementVersion);
		}
	}

	@Transient
	public UsageAgreementTextEntity getUsageAgreementText(String language) {
		for (UsageAgreementTextEntity usageAgreementText : this.usageAgreementTexts) {
			if (usageAgreementText.getLanguage().equals(language))
				return usageAgreementText;
		}
		return null;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = APPLICATION_COLUMN_NAME, insertable = false, updatable = false)
	public ApplicationEntity getApplication() {
		return this.application;
	}

	public void setApplication(ApplicationEntity application) {
		this.application = application;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pk).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof UsageAgreementEntity) {
			return false;
		}
		UsageAgreementEntity rhs = (UsageAgreementEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_WHERE_APPLICATION)
		List<UsageAgreementEntity> listUsageAgreements(
				@QueryParam("application")
				ApplicationEntity application);
	}

}
