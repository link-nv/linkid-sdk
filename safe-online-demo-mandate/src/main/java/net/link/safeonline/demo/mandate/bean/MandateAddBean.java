/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate.bean;

import java.net.ConnectException;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateful;

import net.link.safeonline.demo.mandate.Mandate;
import net.link.safeonline.demo.mandate.MandateAdd;
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.ws.data.DataClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("mandateAdd")
@LocalBinding(jndiBinding = "SafeOnlineMandateDemo/MandateAddBean/local")
@SecurityDomain(MandateConstants.SECURITY_DOMAIN)
public class MandateAddBean extends AbstractMandateDataClientBean implements
		MandateAdd {

	@In
	private String mandateUser;

	@Logger
	private Log log;

	public static final String NEW_MANDATE = "newMandate";

	@In(value = NEW_MANDATE, required = false)
	private Mandate newMandate;

	@In(create = true)
	FacesMessages facesMessages;

	@RolesAllowed(MandateConstants.ADMIN_ROLE)
	public String add() {
		log.debug("add new mandate for user #0", this.mandateUser);

		DataClient dataClient = getDataClient();
		try {
			dataClient.createAttribute(this.mandateUser,
					DemoConstants.MANDATE_ATTRIBUTE_NAME, this.newMandate);
		} catch (ConnectException e) {
			this.facesMessages.add("connection error");
			return null;
		}

		return "success";
	}

	@Factory(NEW_MANDATE)
	public Mandate newMandateFactory() {
		Mandate newMandate = new Mandate();
		return newMandate;
	}
}
