/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class UsageAgreementEntity {

	private static final long serialVersionUID = 1L;

	public static final String APPLICATION_COLUMN_NAME = "application";

	public static final String USAGE_AGREEMENT_VERSION_COLUMN_NAME = "usageAgreementVersion";

	private UsageAgreementPK pk;

	private Set<UsageAgreementTextEntity> usageAgreementTexts;

	public UsageAgreementEntity() {
		// empty
	}

	public UsageAgreementEntity(String application, Long usageAgreementVersion) {
		this.pk = new UsageAgreementPK(application, usageAgreementVersion);
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

	@OneToMany
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
}
