/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription.bean;

import java.net.ConnectException;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;

import net.link.safeonline.demo.prescription.PrescriptionConstants;
import net.link.safeonline.demo.prescription.PrescriptionSearch;
import net.link.safeonline.demo.prescription.UserStatus;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataValue;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("prescriptionSearch")
@LocalBinding(jndiBinding = "SafeOnlinePrescriptionDemo/PrescriptionSearchBean/local")
@SecurityDomain(PrescriptionConstants.SECURITY_DOMAIN)
public class PrescriptionSearchBean extends AbstractPrescriptionDataClientBean
		implements PrescriptionSearch {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@In("name")
	@Out(scope = ScopeType.SESSION)
	private String name;

	@SuppressWarnings("unused")
	@Out(value = "userStatus", scope = ScopeType.SESSION)
	private UserStatus userStatus;

	@RolesAllowed(PrescriptionConstants.ADMIN_ROLE)
	public String search() {
		log.debug("search: " + this.name);

		boolean admin = false;
		boolean careProvider = false;
		boolean pharmacist = false;

		this.userStatus = new UserStatus();
		
		DataClient dataClient = getDataClient();
		try {
			DataValue value = dataClient.getAttributeValue(this.name,
					DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME);
			if (null != value && Boolean.parseBoolean(value.getValue())) {
				admin = true;
			}

			value = dataClient.getAttributeValue(this.name,
					DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME);
			if (null != value && Boolean.parseBoolean(value.getValue())) {
				careProvider = true;
			}

			value = dataClient.getAttributeValue(this.name,
					DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME);
			if (null != value && Boolean.parseBoolean(value.getValue())) {
				pharmacist = true;
			}
		} catch (ConnectException e) {
			this.facesMessages.add("connection error: " + e.getMessage());
			return null;
		} catch (RequestDeniedException e) {
			this.facesMessages.add("request denied");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.add("subject not found");
			return null;
		}

		this.userStatus = new UserStatus(this.name, admin, careProvider,
				pharmacist);

		return "success";
	}
}
