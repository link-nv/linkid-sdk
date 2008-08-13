/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionPatient;
import net.link.safeonline.demo.prescription.entity.PrescriptionEntity;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.log.Log;


@Stateful
@Name("prescriptionPatient")
@LocalBinding(jndiBinding = "SafeOnlinePrescriptionDemo/PrescriptionPatientBean/local")
@SecurityDomain(PrescriptionConstants.SECURITY_DOMAIN)
public class PrescriptionPatientBean implements PrescriptionPatient {

    @Logger
    private Log                      log;

    public final static String       PATIENT_PRESCRIPTIONS = "patientPrescriptions";

    public final static String       SELECTED_PRESCRIPTION = "selectedPrescription";

    @SuppressWarnings("unused")
    @DataModel(PATIENT_PRESCRIPTIONS)
    private List<PrescriptionEntity> prescriptions;

    @DataModelSelection(PATIENT_PRESCRIPTIONS)
    @Out(value = SELECTED_PRESCRIPTION, required = false, scope = ScopeType.SESSION)
    private PrescriptionEntity       selectedPrescription;


    @Remove
    @Destroy
    public void destroyCallback() {

    }


    @PersistenceContext(unitName = PrescriptionConstants.ENTITY_MANAGER)
    private EntityManager  entityManager;

    @Resource
    private SessionContext sessionContext;


    @SuppressWarnings("unchecked")
    @RolesAllowed(PrescriptionConstants.PATIENT_ROLE)
    @Factory(PATIENT_PRESCRIPTIONS)
    public void patientPrescriptionsFactory() {

        Principal patientPrincipal = this.sessionContext.getCallerPrincipal();
        String patient = patientPrincipal.getName();
        Query query = this.entityManager.createQuery("SELECT prescription FROM PrescriptionEntity AS prescription "
                + "WHERE prescription.patient = :patient");
        query.setParameter("patient", patient);
        this.prescriptions = query.getResultList();
    }

    @RolesAllowed(PrescriptionConstants.PATIENT_ROLE)
    public String view() {

        this.log.debug("view: #0", this.selectedPrescription.getId());
        return "view";
    }
}
