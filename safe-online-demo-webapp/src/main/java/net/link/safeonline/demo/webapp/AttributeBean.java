/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.sdk.attrib.AttributeClient;
import net.link.safeonline.sdk.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.attrib.AttributeNotFoundException;

public class AttributeBean {

	private static final Log LOG = LogFactory.getLog(AttributeBean.class);

	private String attributeName;

	private String attributeWebServiceLocation;

	private String attributeValue;

	private String subjectLogin;

	public String getSubjectLogin() {
		return subjectLogin;
	}

	public void setSubjectLogin(String subjectLogin) {
		this.subjectLogin = subjectLogin;
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeWebServiceLocation() {
		return this.attributeWebServiceLocation;
	}

	public void setAttributeWebServiceLocation(
			String attributeWebServiceLocation) {
		this.attributeWebServiceLocation = attributeWebServiceLocation;
	}

	public String getAttributeValue() {
		if (null == this.attributeValue) {
			AttributeClient attributeClient = new AttributeClientImpl(
					this.attributeWebServiceLocation);
			try {
				this.attributeValue = attributeClient.getAttributeValue(
						this.subjectLogin, this.attributeName);
			} catch (AttributeNotFoundException e) {
				LOG.error("attribute not found: " + e.getMessage());
				return "[attribute not found]";
			}
		}
		return this.attributeValue;
	}
}
