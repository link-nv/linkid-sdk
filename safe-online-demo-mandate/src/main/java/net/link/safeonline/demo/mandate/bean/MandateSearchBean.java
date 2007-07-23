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
import net.link.safeonline.demo.mandate.MandateConstants;
import net.link.safeonline.demo.mandate.MandateSearch;
import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.data.Attribute;
import net.link.safeonline.sdk.ws.data.DataClient;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.core.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("mandateSearch")
@LocalBinding(jndiBinding = "SafeOnlineMandateDemo/MandateSearchBean/local")
@SecurityDomain(MandateConstants.SECURITY_DOMAIN)
public class MandateSearchBean extends AbstractMandateDataClientBean implements
		MandateSearch {

	@Logger
	private Log log;

	@In(create = true)
	FacesMessages facesMessages;

	@SuppressWarnings("unused")
	@DataModel
	private Mandate[] mandates;

	@DataModelSelection
	private Mandate selectedMandate;

	@SuppressWarnings("unused")
	@Out(required = false, scope = ScopeType.SESSION)
	@In(required = false)
	private String mandateUser;

	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@RolesAllowed(MandateConstants.ADMIN_ROLE)
	public String search() {
		log.debug("search for #0", this.name);

		DataClient dataClient = getDataClient();
		Attribute<Mandate[]> mandateAttribute;
		try {
			mandateAttribute = dataClient.getAttributeValue(this.name,
					DemoConstants.MANDATE_ATTRIBUTE_NAME, Mandate[].class);
		} catch (ConnectException e) {
			this.facesMessages.add("connection error: " + e.getMessage());
			return null;
		} catch (RequestDeniedException e) {
			this.facesMessages.add("request denied");
			return null;
		} catch (SubjectNotFoundException e) {
			this.facesMessages.addToControl("name", "subject not found");
			return null;
		}

		if (null != mandateAttribute) {
			this.mandates = mandateAttribute.getValue();
		} else {
			this.mandates = new Mandate[] {};
		}

		this.mandateUser = this.name;

		return "success";
	}

	@RolesAllowed(MandateConstants.ADMIN_ROLE)
	public String removeMandate() {
		log.debug("remove mandate : " + this.selectedMandate);
		DataClient dataClient = getDataClient();
		try {
			dataClient.removeAttribute(this.mandateUser,
					DemoConstants.MANDATE_ATTRIBUTE_NAME, this.selectedMandate
							.getAttributeId());
		} catch (ConnectException e) {
			this.facesMessages.add("connection error: " + e.getMessage());
			return null;
		}
		return "success";
	}

}
