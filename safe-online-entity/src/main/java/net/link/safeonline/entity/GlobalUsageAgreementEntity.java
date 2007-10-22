/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class GlobalUsageAgreementEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String GLOBAL_USAGE_AGREEMENT = "GLOBAL_USAGE_AGREEMENT";

	public static final Long DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION = -1L;

	public static final Long EMPTY_GLOBAL_USAGE_AGREEMENT_VERSION = 0L;

	public static final Long INITIAL_GLOBAL_USAGE_AGREEMENT_VERSION = 0L;

	private Long usageAgreementVersion;

	private Set<UsageAgreementTextEntity> usageAgreementTexts;

	public GlobalUsageAgreementEntity() {
		this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
	}

	public GlobalUsageAgreementEntity(Long usageAgreementVersion) {
		this.usageAgreementVersion = usageAgreementVersion;
		this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
	}

	@OneToMany(fetch = FetchType.EAGER)
	public Set<UsageAgreementTextEntity> getUsageAgreementTexts() {
		return this.usageAgreementTexts;
	}

	public void setUsageAgreementTexts(
			Set<UsageAgreementTextEntity> usageAgreementTexts) {
		this.usageAgreementTexts = usageAgreementTexts;
	}

	@Id
	public Long getUsageAgreementVersion() {
		return this.usageAgreementVersion;
	}

	public void setUsageAgreementVersion(Long usageAgreementVersion) {
		this.usageAgreementVersion = usageAgreementVersion;
	}

	@Transient
	public UsageAgreementTextEntity getUsageAgreementText(String language) {
		for (UsageAgreementTextEntity usageAgreementText : this.usageAgreementTexts) {
			if (usageAgreementText.getLanguage().equals(language))
				return usageAgreementText;
		}
		return null;
	}
}
