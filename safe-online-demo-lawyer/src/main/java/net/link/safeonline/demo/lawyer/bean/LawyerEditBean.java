/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer.bean;

import java.net.ConnectException;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

import net.link.safeonline.demo.lawyer.LawyerConstants;
import net.link.safeonline.demo.lawyer.LawyerEdit;
import net.link.safeonline.demo.lawyer.LawyerStatus;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;

@Stateful
@Name("lawyerEdit")
@LocalBinding(jndiBinding = "SafeOnlineLawyerDemo/LawyerEditBean/local")
@SecurityDomain(LawyerConstants.SECURITY_DOMAIN)
public class LawyerEditBean extends AbstractLawyerDataClientBean implements
		LawyerEdit {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@In("name")
	@Out("name")
	private String name;

	@SuppressWarnings("unused")
	@In("lawyerEditableStatus")
	@Out("lawyerEditableStatus")
	private LawyerStatus lawyerStatus;

	@RolesAllowed(LawyerConstants.ADMIN_ROLE)
	public String persist() {
		log
				.debug(
						"---------------------------------------- save #0 -----------------------------",
						this.name);

		try {
			createOrUpdateAttribute(DemoConstants.LAWYER_ATTRIBUTE_NAME,
					Boolean.toString(this.lawyerStatus.isLawyer()));
			createOrUpdateAttribute(
					DemoConstants.LAWYER_SUSPENDED_ATTRIBUTE_NAME, Boolean
							.toString(this.lawyerStatus.isSuspended()));
			createOrUpdateAttribute(DemoConstants.LAWYER_BAR_ATTRIBUTE_NAME,
					this.lawyerStatus.getBar());
			createOrUpdateAttribute(
					DemoConstants.LAWYER_BAR_ADMIN_ATTRIBUTE_NAME, Boolean
							.toString(this.lawyerStatus.isBarAdmin()));
		} catch (ConnectException e) {
			this.facesMessages.add("connection error");
			return null;
		} catch (RequestDeniedException e) {
			this.facesMessages.add("request denied");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.add("subject not found: " + this.name);
			return null;
		}
		return "success";
	}

	private void createOrUpdateAttribute(String attributeName,
			String attributeValue) throws ConnectException,
			RequestDeniedException, SubjectNotFoundException {
		DataClient dataClient = getDataClient();
		if (null == dataClient.getAttributeValue(this.name, attributeName)) {
			log.debug("create attribute #0 for #1", attributeName, this.name);
			dataClient.createAttribute(this.name, attributeName);
		}
		log.debug("set attribute #0 for #1", attributeName, this.name);
		dataClient.setAttributeValue(this.name, attributeName, attributeValue);
	}
}
