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
import net.link.safeonline.demo.prescription.PrescriptionEdit;
import net.link.safeonline.demo.prescription.UserStatus;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;

@Stateful
@Name("prescriptionEdit")
@LocalBinding(jndiBinding = "SafeOnlinePrescriptionDemo/PrescriptionEditBean/local")
@SecurityDomain(PrescriptionConstants.SECURITY_DOMAIN)
public class PrescriptionEditBean extends AbstractPrescriptionDataClientBean
		implements PrescriptionEdit {

	@Logger
	private Log log;

	@In("userStatus")
	@Out("userStatus")
	private UserStatus userStatus;

	@RolesAllowed(PrescriptionConstants.ADMIN_ROLE)
	public String persist() {
		try {
			createOrUpdateAttribute(
					DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME, Boolean
							.valueOf(this.userStatus.isAdmin()), Boolean.class);
			createOrUpdateAttribute(
					DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME,
					Boolean.valueOf(this.userStatus.isCareProvider()), Boolean.class);
			createOrUpdateAttribute(
					DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME,
					Boolean.valueOf(this.userStatus.isPharmacist()), Boolean.class);
		} catch (ConnectException e) {
			this.facesMessages.add("connection error");
			return null;
		} catch (RequestDeniedException e) {
			this.facesMessages.add("request denied");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.add("subject not found: "
					+ this.userStatus.getName());
			return null;
		} catch (AttributeNotFoundException e) {
			this.facesMessages.add("attribute not found");
			return null;
		}
		return "success";
	}

	private void createOrUpdateAttribute(String attributeName,
			Object attributeValue, Class<?> expectedClass) throws ConnectException,
			RequestDeniedException, SubjectNotFoundException,
			AttributeNotFoundException {
		DataClient dataClient = getDataClient();
		String userId = this.userStatus.getUserId();
		if (null == dataClient.getAttributeValue(userId, attributeName, expectedClass)) {
			this.log.debug("create attribute #0 for #1", attributeName, userId);
			dataClient.createAttribute(userId, attributeName, attributeValue);
		} else {
			this.log.debug("set attribute #0 for #1", attributeName, userId);
			dataClient.setAttributeValue(userId, attributeName, attributeValue);
		}
	}
}
