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
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;

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

		this.userStatus = new UserStatus();

		String userId;
		NameIdentifierMappingClient mappingClient = super.getMappingClient();
		try {
			userId = mappingClient.getUserId(this.name);
		} catch (SubjectNotFoundException e) {
			log.debug("subject not found: #0", this.name);
			this.facesMessages.add("subject not found");
			return null;
		} catch (RequestDeniedException e) {
			log.debug("request denied");
			this.facesMessages.add("request denied");
			return null;
		}

		boolean admin = false;
		boolean careProvider = false;
		boolean pharmacist = false;

		DataClient dataClient = getDataClient();
		try {
			Attribute<Boolean> adminAttribute = dataClient.getAttributeValue(
					userId, DemoConstants.PRESCRIPTION_ADMIN_ATTRIBUTE_NAME,
					Boolean.class);
			if (null != adminAttribute) {
				Boolean value = adminAttribute.getValue();
				if (null != value) {
					admin = value;
				}
			}

			Attribute<Boolean> careProviderAttribute = dataClient
					.getAttributeValue(
							userId,
							DemoConstants.PRESCRIPTION_CARE_PROVIDER_ATTRIBUTE_NAME,
							Boolean.class);
			if (null != careProviderAttribute) {
				Boolean value = careProviderAttribute.getValue();
				if (null != value) {
					careProvider = value;
				}
			}

			Attribute<Boolean> pharmacistAttribute = dataClient
					.getAttributeValue(
							userId,
							DemoConstants.PRESCRIPTION_PHARMACIST_ATTRIBUTE_NAME,
							Boolean.class);
			if (null != pharmacistAttribute) {
				Boolean value = pharmacistAttribute.getValue();
				if (null != value) {
					pharmacist = value;
				}
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

		this.userStatus = new UserStatus(this.name, userId, admin,
				careProvider, pharmacist);

		return "success";
	}
}
