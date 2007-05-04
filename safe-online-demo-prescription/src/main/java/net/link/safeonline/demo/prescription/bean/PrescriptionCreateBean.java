/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionCreate;
import net.link.safeonline.demo.prescription.entity.PrescriptionEntity;
import net.link.safeonline.demo.prescription.entity.PrescriptionMedicineEntity;

@Stateful
@Name("prescriptionCreate")
@LocalBinding(jndiBinding = "SafeOnlinePrescriptionDemo/PrescriptionCreateBean/local")
@SecurityDomain(PrescriptionConstants.SECURITY_DOMAIN)
public class PrescriptionCreateBean implements PrescriptionCreate {

	@Logger
	private Log log;

	@Remove
	@Destroy
	public void destroyCallback() {
	}

	@RolesAllowed(PrescriptionConstants.CARE_PROVIDER_ROLE)
	@Factory("medicines")
	public List<SelectItem> medicinesFactory() {
		List<SelectItem> medicines = new LinkedList<SelectItem>();
		medicines.add(new SelectItem("Acomplia"));
		medicines.add(new SelectItem("Actilyse"));
		medicines.add(new SelectItem("Advair"));
		medicines.add(new SelectItem("Advil"));
		medicines.add(new SelectItem("Buscopan"));
		medicines.add(new SelectItem("Prozac"));
		medicines.add(new SelectItem("Viagra"));
		return medicines;
	}

	private List<String> selectedMedicines;

	public List<String> getSelectedMedicines() {
		return this.selectedMedicines;
	}

	public void setSelectedMedicines(List<String> selectedMedicines) {
		this.selectedMedicines = selectedMedicines;
	}

	@In(value = "patient", required = false)
	private String patient;

	@PersistenceContext(unitName = "DemoPrescriptionEntityManager")
	private EntityManager entityManager;

	@Resource
	private SessionContext sessionContext;

	@RolesAllowed(PrescriptionConstants.CARE_PROVIDER_ROLE)
	public String create() {
		log.debug("create prescription for patient: #0", this.patient);
		Principal careProviderPrincipal = this.sessionContext
				.getCallerPrincipal();
		String careProvider = careProviderPrincipal.getName();
		PrescriptionEntity prescription = new PrescriptionEntity(this.patient,
				careProvider);
		this.entityManager.persist(prescription);
		log.debug("prescription id: #0", prescription.getId());
		for (String selectedMedicine : this.selectedMedicines) {
			PrescriptionMedicineEntity prescriptionMedicine = new PrescriptionMedicineEntity(
					prescription, selectedMedicine);
			this.entityManager.persist(prescriptionMedicine);
		}
		return "created";
	}
}
