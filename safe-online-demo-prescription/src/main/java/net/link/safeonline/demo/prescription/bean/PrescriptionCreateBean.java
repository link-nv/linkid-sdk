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
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionCreate;
import net.link.safeonline.demo.prescription.entity.PrescriptionEntity;
import net.link.safeonline.demo.prescription.entity.PrescriptionMedicineEntity;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;


@Stateful
@Name("prescriptionCreate")
@LocalBinding(jndiBinding = "SafeOnlinePrescriptionDemo/PrescriptionCreateBean/local")
@SecurityDomain(PrescriptionConstants.SECURITY_DOMAIN)
public class PrescriptionCreateBean extends AbstractPrescriptionDataClientBean implements PrescriptionCreate {

    @Logger
    private Log log;


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
    private String         patient;

    @PersistenceContext(unitName = PrescriptionConstants.ENTITY_MANAGER)
    private EntityManager  entityManager;

    @Resource
    private SessionContext sessionContext;


    @RolesAllowed(PrescriptionConstants.CARE_PROVIDER_ROLE)
    public String create() {

        this.log.debug("create prescription for patient: #0", this.patient);
        Principal careProviderPrincipal = this.sessionContext.getCallerPrincipal();
        String careProvider = careProviderPrincipal.getName();
        String careProviderName = super.getUsername(careProvider);
        String patientName = super.getUsername(this.patient);

        PrescriptionEntity prescription = new PrescriptionEntity(this.patient, patientName, careProvider,
                careProviderName);
        this.entityManager.persist(prescription);
        this.log.debug("prescription id: #0", prescription.getId());
        for (String selectedMedicine : this.selectedMedicines) {
            PrescriptionMedicineEntity prescriptionMedicine = new PrescriptionMedicineEntity(prescription,
                    selectedMedicine);
            this.entityManager.persist(prescriptionMedicine);
        }
        return "created";
    }

    @Factory("patientName")
    public String patientNameFactory() {

        String patientName = super.getUsername(this.patient);
        return patientName;
    }
}
