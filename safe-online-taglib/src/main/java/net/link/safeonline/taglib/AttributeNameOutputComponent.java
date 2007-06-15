/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.io.IOException;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import net.link.safeonline.authentication.service.AttributeDO;

/**
 * JSF output component for name of {@link AttributeDO}. The visual effect will
 * depend on the category of the attribute. A compounded entry will appear in
 * bold, a member entry will appear in italic.
 * 
 * @author fcorneli
 * 
 */
public class AttributeNameOutputComponent extends UIOutput {

	public static final String COMPONENT_TYPE = "net.link.component.attributeNameOutput";

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter response = context.getResponseWriter();
		response.startElement("span", this);
		String clientId = getClientId(context);
		response.writeAttribute("id", clientId, "id");
		AttributeDO attribute = (AttributeDO) getValue();
		if (attribute.isCompounded()) {
			response.startElement("b", null);
			response.write(attribute.getHumanReadableName());
			response.endElement("b");
		} else if (attribute.isMember()) {
			response.startElement("i", null);
			response.write(attribute.getHumanReadableName());
			response.endElement("i");
		} else {
			response.write(attribute.getHumanReadableName());
		}
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		ResponseWriter response = context.getResponseWriter();
		response.endElement("span");
	}
}
