/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionLogon;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateless
@Name("prescriptionLogon")
@LocalBinding(jndiBinding = "SafeOnlinePrescriptionDemo/PrescriptionLogonBean/local")
public class PrescriptionLogonBean implements PrescriptionLogon {

	@Logger
	private Log log;

	@In(create = true)
	private FacesMessages facesMessages;

	public String login() {
		log.debug("login");
		String result = SafeOnlineLoginUtils.login(this.facesMessages,
				this.log, "login");
		return result;
	}

	public String logout() {
		log.debug("logout");
		Seam.invalidateSession();
		return "logout-success";
	}

	private void activateRole(String role) {
		FacesContext context = FacesContext.getCurrentInstance();
		context.getExternalContext().getSessionMap().put("role", role);
	}

	public String activateAdminRole() {
		activateRole(PrescriptionConstants.ADMIN_ROLE);
		return "admin";
	}

	public String activateCareProviderRole() {
		activateRole(PrescriptionConstants.CARE_PROVIDER_ROLE);
		return "careProvider";
	}

	public String activatePharmacistRole() {
		activateRole(PrescriptionConstants.PHARMACIST_ROLE);
		return "pharmacist";
	}
}
