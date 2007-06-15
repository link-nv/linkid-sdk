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
import org.apache.commons.lang.builder.ToStringStyle;

@Embeddable
public class ApplicationIdentityAttributePK implements Serializable {

	public static final long INITIAL_IDENTITY_VERSION = 1;

	private static final long serialVersionUID = 1L;

	private String application;

	private long identityVersion;

	private String attributeTypeName;

	public ApplicationIdentityAttributePK() {
		// empty
	}

	public ApplicationIdentityAttributePK(String application,
			long identityVersion, String attributeTypeName) {
		this.application = application;
		this.identityVersion = identityVersion;
		this.attributeTypeName = attributeTypeName;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public long getIdentityVersion() {
		return this.identityVersion;
	}

	public void setIdentityVersion(long identityVersion) {
		this.identityVersion = identityVersion;
	}

	public String getAttributeTypeName() {
		return this.attributeTypeName;
	}

	public void setAttributeTypeName(String attributeTypeName) {
		this.attributeTypeName = attributeTypeName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof ApplicationIdentityAttributePK) {
			return false;
		}
		ApplicationIdentityAttributePK rhs = (ApplicationIdentityAttributePK) obj;
		return new EqualsBuilder().append(this.application, rhs.application)
				.append(this.identityVersion, rhs.identityVersion).append(
						this.attributeTypeName, rhs.attributeTypeName)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.application).append(
				this.identityVersion).append(this.attributeTypeName)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("application", this.application).append(
						"identityVersion", this.identityVersion).append(
						"attributeType", this.attributeTypeName).toString();
	}
}
