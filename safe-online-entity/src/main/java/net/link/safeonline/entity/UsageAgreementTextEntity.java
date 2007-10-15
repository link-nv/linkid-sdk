/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

@Entity
public class UsageAgreementTextEntity {

	private static final long serialVersionUID = 1L;

	public static final String APPLICATION_COLUMN_NAME = "application";

	public static final String USAGE_AGREEMENT_VERSION_COLUMN_NAME = "usageAgreementVersion";

	public static final String LANGUAGE_COLUMN_NAME = "language";

	private String text;

	private UsageAgreementTextPK pk;

	public UsageAgreementTextEntity() {
		// empty
	}

	public UsageAgreementTextEntity(UsageAgreementEntity usageAgreement,
			String text, String language) {
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

}
