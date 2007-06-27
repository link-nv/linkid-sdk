/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.bean;

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

import net.link.safeonline.demo.payment.PaymentConstants;
import net.link.safeonline.demo.payment.CustomerEdit;
import net.link.safeonline.demo.payment.CustomerStatus;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.DataClient;

@Stateful
@Name("customerEdit")
@LocalBinding(jndiBinding = "SafeOnlinePaymentDemo/CustomerEditBean/local")
@SecurityDomain(PaymentConstants.SECURITY_DOMAIN)
public class CustomerEditBean extends AbstractPaymentDataClientBean implements
		CustomerEdit {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@In("name")
	@Out("name")
	private String name;

	@SuppressWarnings("unused")
	@In("customerEditableStatus")
	@Out("customerEditableStatus")
	private CustomerStatus customerStatus;

	@RolesAllowed(PaymentConstants.ADMIN_ROLE)
	public String persist() {
		log
				.debug(
						"---------------------------------------- save #0 -----------------------------",
						this.name);

		try {
			createOrUpdateAttribute(
					DemoConstants.PAYMENT_JUNIOR_ATTRIBUTE_NAME, Boolean
							.valueOf(this.customerStatus.isJunior()));
			createOrUpdateAttribute(DemoConstants.PAYMENT_ADMIN_ATTRIBUTE_NAME,
					Boolean.valueOf(this.customerStatus.isPaymentAdmin()));
		} catch (ConnectException e) {
			this.facesMessages.add("connection error");
			return null;
		} catch (RequestDeniedException e) {
			this.facesMessages.add("request denied");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.add("subject not found: " + this.name);
			return null;
		} catch (AttributeNotFoundException e) {
			this.facesMessages.add("attribute not found");
			return null;
		}
		return "success";
	}

	private void createOrUpdateAttribute(String attributeName,
			Object attributeValue) throws ConnectException,
			RequestDeniedException, SubjectNotFoundException,
			AttributeNotFoundException {
		DataClient dataClient = getDataClient();
		if (null == dataClient.getAttributeValue(this.name, attributeName,
				attributeValue.getClass())) {
			log.debug("create attribute #0 for #1", attributeName, this.name);
			dataClient
					.createAttribute(this.name, attributeName, attributeValue);
		} else {
			log.debug("set attribute #0 for #1", attributeName, this.name);
			dataClient.setAttributeValue(this.name, attributeName,
					attributeValue);
		}
	}
}
