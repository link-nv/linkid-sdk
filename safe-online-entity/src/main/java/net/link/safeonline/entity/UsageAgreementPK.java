/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
public class UsageAgreementPK implements Serializable {

	public static final Long DRAFT_USAGE_AGREEMENT_VERSION = -1L;

	public static final Long EMPTY_USAGE_AGREEMENT_VERSION = 0L;

	public static final Long INITIAL_USAGE_AGREEMENT_VERSION = 1L;

	private static final long serialVersionUID = 1L;

	private String application;

	private Long usageAgreementVersion;

	public UsageAgreementPK() {
		// empty
	}

	public UsageAgreementPK(String application, Long usageAgreementVersion) {
		this.application = application;
		this.usageAgreementVersion = usageAgreementVersion;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Long getUsageAgreementVersion() {
		return this.usageAgreementVersion;
	}

	public void setUsageAgreementVersion(Long usageAgreementVersion) {
		this.usageAgreementVersion = usageAgreementVersion;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof UsageAgreementPK) {
			return false;
		}
		UsageAgreementPK rhs = (UsageAgreementPK) obj;
		return new EqualsBuilder().append(this.application, rhs.application)
				.append(this.usageAgreementVersion, rhs.usageAgreementVersion)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.application).append(
				this.usageAgreementVersion).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("application", this.application).append(
						"usageAgreementVersion", this.usageAgreementVersion)
				.toString();
	}

}
