/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Embeddable
public class PrescriptionMedicinePK implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;

	private String name;

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PrescriptionMedicinePK() {
		// empty
	}

	public PrescriptionMedicinePK(long id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof PrescriptionMedicinePK) {
			return false;
		}
		PrescriptionMedicinePK rhs = (PrescriptionMedicinePK) obj;
		return new EqualsBuilder().append(this.id, rhs.id).append(this.name,
				rhs.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).append(this.name)
				.toHashCode();
	}
}
