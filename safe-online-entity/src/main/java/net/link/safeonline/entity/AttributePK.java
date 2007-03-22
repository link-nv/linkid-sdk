/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
public class AttributePK implements Serializable {

	private static final long serialVersionUID = 1L;

	private String attributeType;

	private String subject;

	public AttributePK() {
		// empty
	}

	public AttributePK(String attributeType, String subject) {
		this.attributeType = attributeType;
		this.subject = subject;
	}

	public String getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof SubscriptionPK) {
			return false;
		}
		AttributePK rhs = (AttributePK) obj;
		return new EqualsBuilder().append(this.subject, rhs.subject).append(
				this.attributeType, rhs.attributeType).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.subject).append(
				this.attributeType).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("subject", this.subject)
				.append("attributeType", this.attributeType).toString();
	}
}
