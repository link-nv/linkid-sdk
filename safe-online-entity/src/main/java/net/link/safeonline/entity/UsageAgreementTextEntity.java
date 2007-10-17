/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
public class UsageAgreementTextEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String APPLICATION_COLUMN_NAME = "application";

	public static final String USAGE_AGREEMENT_VERSION_COLUMN_NAME = "usageAgreementVersion";

	public static final String LANGUAGE_COLUMN_NAME = "language";

	private String text;

	private UsageAgreementTextPK pk;

	private UsageAgreementEntity usageAgreement;

	public UsageAgreementTextEntity() {
		// empty
	}

	public UsageAgreementTextEntity(UsageAgreementEntity usageAgreement,
			String text, String language) {
		this.usageAgreement = usageAgreement;
		this.text = text;
		this.pk = new UsageAgreementTextPK(usageAgreement, language);
	}

	@Transient
	public String getLanguage() {
		return this.pk.getLanguage();
	}

	@Lob
	@Column(length = 1024 * 1024)
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "application", column = @Column(name = APPLICATION_COLUMN_NAME)),
			@AttributeOverride(name = "usageAgreement", column = @Column(name = USAGE_AGREEMENT_VERSION_COLUMN_NAME)),
			@AttributeOverride(name = "language", column = @Column(name = LANGUAGE_COLUMN_NAME)) })
	public UsageAgreementTextPK getPk() {
		return this.pk;
	}

	public void setPk(UsageAgreementTextPK pk) {
		this.pk = pk;
	}

	@ManyToOne(optional = false)
	@JoinColumns( {
			@JoinColumn(name = APPLICATION_COLUMN_NAME, insertable = false, updatable = false, referencedColumnName = UsageAgreementEntity.APPLICATION_COLUMN_NAME),
			@JoinColumn(name = USAGE_AGREEMENT_VERSION_COLUMN_NAME, insertable = false, updatable = false, referencedColumnName = UsageAgreementEntity.USAGE_AGREEMENT_VERSION_COLUMN_NAME) })
	public UsageAgreementEntity getUsageAgreement() {
		return usageAgreement;
	}

	public void setUsageAgreement(UsageAgreementEntity usageAgreement) {
		this.usageAgreement = usageAgreement;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof UsageAgreementTextEntity) {
			return false;
		}
		UsageAgreementTextEntity rhs = (UsageAgreementTextEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pk).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("pk", this.pk).append("text", this.text).toString();
	}

}
