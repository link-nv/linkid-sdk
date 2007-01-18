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
public class TrustPointPK implements Serializable {

	private static final long serialVersionUID = 1L;

	private String subjectName;

	private long domain;

	public TrustPointPK() {
		// empty
	}

	public TrustPointPK(TrustDomainEntity trustDomain, String subjectName) {
		this.domain = trustDomain.getId();
		this.subjectName = subjectName;
	}

	public String getSubjectName() {
		return this.subjectName;
	}

	public void setSubjectName(String application) {
		this.subjectName = application;
	}

	public long getDomain() {
		return this.domain;
	}

	public void setDomain(long domain) {
		this.domain = domain;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof TrustPointPK) {
			return false;
		}
		TrustPointPK rhs = (TrustPointPK) obj;
		return new EqualsBuilder().append(this.domain, rhs.domain).append(
				this.subjectName, rhs.subjectName).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.domain).append(
				this.subjectName).hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("domain", this.domain).append(
				"subject name", this.subjectName).toString();
	}
}
