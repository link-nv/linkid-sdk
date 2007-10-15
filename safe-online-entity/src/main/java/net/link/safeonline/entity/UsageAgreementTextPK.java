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
public class UsageAgreementTextPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private String application;

	private Long usageAgreementVersion;

	private String language;

	public UsageAgreementTextPK() {
		// empty
	}

	public UsageAgreementTextPK(UsageAgreementEntity usageAgreement,
			String language) {
		this.language = language;
		this.usageAgreementVersion = usageAgreement.getPk()
				.getUsageAgreementVersion();
		this.application = usageAgreement.getPk().getApplication();
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Long getUsageAgreementVersion() {
		return usageAgreementVersion;
	}

	public void setUsageAgreementVersion(Long usageAgreementVersion) {
		this.usageAgreementVersion = usageAgreementVersion;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof UsageAgreementTextPK) {
			return false;
		}
		UsageAgreementTextPK rhs = (UsageAgreementTextPK) obj;
		return new EqualsBuilder().append(this.usageAgreementVersion,
				rhs.usageAgreementVersion).append(this.application,
				rhs.application).append(this.language, rhs.language).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.application).append(
				this.usageAgreementVersion).append(this.language).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(this.application).append(
				this.usageAgreementVersion.toString()).append(this.language)
				.toString();
	}
}
