/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.entity;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "prescription_medicines")
public class PrescriptionMedicineEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private PrescriptionMedicinePK pk;

	public static final String ID_COLUMN_NAME = "id";

	public static final String NAME_COLUMN_NAME = "name";

	private String name;

	private PrescriptionEntity prescription;

	@EmbeddedId
	@AttributeOverrides( {
			@AttributeOverride(name = "id", column = @Column(name = ID_COLUMN_NAME)),
			@AttributeOverride(name = "name", column = @Column(name = NAME_COLUMN_NAME)) })
	public PrescriptionMedicinePK getPk() {
		return this.pk;
	}

	public void setPk(PrescriptionMedicinePK pk) {
		this.pk = pk;
	}

	@Column(name = NAME_COLUMN_NAME, insertable = false, updatable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = ID_COLUMN_NAME, insertable = false, updatable = false)
	public PrescriptionEntity getPrescription() {
		return this.prescription;
	}

	public void setPrescription(PrescriptionEntity prescription) {
		this.prescription = prescription;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (false == obj instanceof PrescriptionMedicineEntity) {
			return false;
		}
		PrescriptionMedicineEntity rhs = (PrescriptionMedicineEntity) obj;
		return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.pk).toHashCode();
	}

	public PrescriptionMedicineEntity() {
		// empty
	}

	public PrescriptionMedicineEntity(PrescriptionEntity prescription,
			String name) {
		this.prescription = prescription;
		this.name = name;
		this.pk = new PrescriptionMedicinePK(this.prescription.getId(),
				this.name);
	}
}
