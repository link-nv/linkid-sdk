/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "prescriptions")
public class PrescriptionEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	private Date creationDate;

	private String patient;

	private String careProvider;

	private boolean filled;

	private List<PrescriptionMedicineEntity> medicines;

	public PrescriptionEntity(String patient, String careProvider) {
		this.patient = patient;
		this.careProvider = careProvider;
		this.filled = false;
		this.creationDate = new Date();
	}

	public PrescriptionEntity() {
		// empty
	}

	public String getCareProvider() {
		return this.careProvider;
	}

	public void setCareProvider(String careProvider) {
		this.careProvider = careProvider;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isFilled() {
		return this.filled;
	}

	public void setFilled(boolean filled) {
		this.filled = filled;
	}

	public String getPatient() {
		return this.patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	@OneToMany(mappedBy = "prescription", cascade = CascadeType.PERSIST)
	public List<PrescriptionMedicineEntity> getMedicines() {
		return this.medicines;
	}

	public void setMedicines(List<PrescriptionMedicineEntity> medicines) {
		this.medicines = medicines;
	}
}
