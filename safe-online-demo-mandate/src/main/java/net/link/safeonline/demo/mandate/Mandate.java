/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.mandate;

import net.link.safeonline.model.demo.DemoConstants;
import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundId;
import net.link.safeonline.sdk.ws.annotation.CompoundMember;

@Compound(DemoConstants.MANDATE_ATTRIBUTE_NAME)
public class Mandate {

	private String companyName;

	private String title;

	private String attributeId;

	@CompoundId
	public String getAttributeId() {
		return this.attributeId;
	}

	public void setAttributeId(String attributeId) {
		this.attributeId = attributeId;
	}

	@CompoundMember(DemoConstants.MANDATE_COMPANY_ATTRIBUTE_NAME)
	public String getCompanyName() {
		return this.companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@CompoundMember(DemoConstants.MANDATE_TITLE_ATTRIBUTE_NAME)
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
