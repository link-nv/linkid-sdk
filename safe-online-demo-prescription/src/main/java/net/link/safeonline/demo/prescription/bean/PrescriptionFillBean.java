/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionFill;
import net.link.safeonline.demo.prescription.entity.PrescriptionEntity;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.log.Log;

@Stateful
@Name("prescriptionFill")
@LocalBinding(jndiBinding = "SafeOnlinePrescriptionDemo/PrescriptionFillBean/local")
@SecurityDomain(PrescriptionConstants.SECURITY_DOMAIN)
public class PrescriptionFillBean extends AbstractPrescriptionDataClientBean
		implements PrescriptionFill {

	@Logger
	private Log log;

	public final static String PRESCRIPTIONS = "prescriptions";

	public final static String SELECTED_PRESCRIPTION = "selectedPrescription";

	@SuppressWarnings("unused")
	@DataModel(PRESCRIPTIONS)
	private List<PrescriptionEntity> prescriptions;

	@DataModelSelection(PRESCRIPTIONS)
	@Out(value = SELECTED_PRESCRIPTION, required = false, scope = ScopeType.SESSION)
	private PrescriptionEntity selectedPrescription;

	/**
	 * Fill prescription entity. We have to use a field different from
	 * selectedPrescription because the DataModelSelection annotation is causing
	 * the selectedPrescription field to become null.
	 */
	@In(value = SELECTED_PRESCRIPTION, required = false)
	private PrescriptionEntity fillPrescription;

	@PersistenceContext(unitName = PrescriptionConstants.ENTITY_MANAGER)
	private EntityManager entityManager;

	@In(value = "patient")
	private String patient;

	@SuppressWarnings("unchecked")
	@RolesAllowed(PrescriptionConstants.PHARMACIST_ROLE)
	@Factory(PRESCRIPTIONS)
	public void prescriptionsFactory() {
		Query query = this.entityManager
				.createQuery("SELECT prescription FROM PrescriptionEntity AS prescription "
						+ "WHERE prescription.patient = :patient AND "
						+ "prescription.filled = FALSE");
		query.setParameter("patient", this.patient);
		this.prescriptions = query.getResultList();
	}

	@RolesAllowed(PrescriptionConstants.PHARMACIST_ROLE)
	public String view() {
		this.log.debug("view: #0", this.selectedPrescription.getId());
		return "view";
	}

	@Resource
	private SessionContext sessionContext;

	@RolesAllowed(PrescriptionConstants.PHARMACIST_ROLE)
	public String fill() {
		this.log.debug("filling: #0", this.fillPrescription.getId());
		this.fillPrescription.setFilled(true);
		Principal pharmacistPrincipal = this.sessionContext
				.getCallerPrincipal();
		String pharmacist = pharmacistPrincipal.getName();
		this.fillPrescription.setPharmacist(pharmacist);

		String pharmacistName = super.getUsername(pharmacist);
		this.fillPrescription.setPharmacistName(pharmacistName);

		this.fillPrescription.setFilledDate(new Date());
		this.entityManager.merge(this.fillPrescription);
		return "filled";
	}
}
