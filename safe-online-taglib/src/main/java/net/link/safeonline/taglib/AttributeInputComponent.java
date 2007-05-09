/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.AttributeDO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF input component for {@link AttributeDO}.
 * 
 * @author fcorneli
 * 
 */
public class AttributeInputComponent extends UIInput {

	public static final String COMPONENT_TYPE = "net.link.component.attributeInput";

	private static final Log LOG = LogFactory
			.getLog(AttributeInputComponent.class);

	public AttributeInputComponent() {
		setRendererType(null);
	}

	@Override
	public void decode(FacesContext context) {
		LOG.debug("decode");

		AttributeDO attribute = (AttributeDO) getValue();
		String type = attribute.getType();
		Renderer renderer = renderers.get(type);
		if (null == renderer) {
			super.decode(context);
			return;
		}

		renderer.decode(context, this);
		super.decode(context);
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		LOG.debug("encodeBegin");
		if (false == isRendered()) {
			return;
		}

		AttributeDO attribute = (AttributeDO) getValue();
		String type = attribute.getType();
		Renderer renderer = renderers.get(type);
		if (null == renderer) {
			ResponseWriter responseWriter = context.getResponseWriter();
			responseWriter.write("Unsupported type: " + type);
			return;
		}

		renderer.encodeBegin(context, this);
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		LOG.debug("encodeEnd");
		if (false == isRendered()) {
			return;
		}

		AttributeDO attribute = (AttributeDO) getValue();
		String type = attribute.getType();
		Renderer renderer = renderers.get(type);
		if (null == renderer) {
			return;
		}

		renderer.encodeEnd(context, this);
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		// do not render children
	}

	/**
	 * This is the lightweight version of JSF Renderer interface.
	 * 
	 * @author fcorneli
	 * 
	 */
	private interface Renderer {
		void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException;

		void encodeEnd(FacesContext context, UIInput inputComponent)
				throws IOException;

		void decode(FacesContext context, UIInput inputComponent);
	}

	private static final Map<String, Renderer> renderers;

	static {
		renderers = new HashMap<String, Renderer>();
		renderers.put(SafeOnlineConstants.STRING_TYPE, new StringRenderer());
		renderers.put(SafeOnlineConstants.BOOLEAN_TYPE, new BooleanRenderer());
	}

	private static class StringRenderer implements Renderer {

		public void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException {
			String clientId = inputComponent.getClientId(context);
			ResponseWriter responseWriter = context.getResponseWriter();

			responseWriter.startElement("input", inputComponent);
			responseWriter.writeAttribute("id", clientId, "id");
			responseWriter.writeAttribute("type", "text", null);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			String value = attribute.getStringValue();

			responseWriter.writeAttribute("name", clientId, null);
			responseWriter.writeAttribute("value", value, null);
		}

		public void encodeEnd(FacesContext context, UIInput inputComponent)
				throws IOException {
			ResponseWriter responseWriter = context.getResponseWriter();

			responseWriter.endElement("input");
		}

		@SuppressWarnings("unchecked")
		public void decode(FacesContext context, UIInput inputComponent) {
			String clientId = inputComponent.getClientId(context);
			ExternalContext externalContext = context.getExternalContext();
			Map<String, String> requestParameterMap = externalContext
					.getRequestParameterMap();
			String decodedValue = requestParameterMap.get(clientId);

			LOG.debug("decoded value: " + decodedValue);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			AttributeDO newAttribute = attribute.clone();

			newAttribute.setStringValue(decodedValue);

			inputComponent.setSubmittedValue(newAttribute);
		}
	}

	private static class BooleanRenderer implements Renderer {

		public void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException {
			String clientId = inputComponent.getClientId(context);
			ResponseWriter responseWriter = context.getResponseWriter();

			responseWriter.startElement("input", inputComponent);
			responseWriter.writeAttribute("id", clientId, "id");
			responseWriter.writeAttribute("type", "checkbox", null);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			Boolean value = attribute.getBooleanValue();

			responseWriter.writeAttribute("name", clientId, null);
			responseWriter.writeAttribute("value", "true", null);
			if (Boolean.TRUE.equals(value)) {
				responseWriter.writeAttribute("checked", "true", null);
			}
		}

		public void encodeEnd(FacesContext context, UIInput inputComponent)
				throws IOException {
			ResponseWriter responseWriter = context.getResponseWriter();

			responseWriter.endElement("input");
		}

		@SuppressWarnings("unchecked")
		public void decode(FacesContext context, UIInput inputComponent) {
			String clientId = inputComponent.getClientId(context);
			ExternalContext externalContext = context.getExternalContext();
			Map<String, String> requestParameterMap = externalContext
					.getRequestParameterMap();
			String decodedValue = requestParameterMap.get(clientId);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			AttributeDO newAttribute = attribute.clone();

			newAttribute.setBooleanValue(Boolean.parseBoolean(decodedValue));

			inputComponent.setSubmittedValue(newAttribute);
		}
	}
}
