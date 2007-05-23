/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.AttributeDO;

/**
 * JSF output component for {@link AttributeDO}.
 * 
 * @author fcorneli
 * 
 */
public class AttributeOutputComponent extends UIOutput {

	public static final String COMPONENT_TYPE = "net.link.component.attributeOutput";

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter response = context.getResponseWriter();
		response.startElement("span", this);
		String clientId = getClientId(context);
		response.writeAttribute("id", clientId, "id");
		AttributeDO attribute = (AttributeDO) getValue();
		String type = attribute.getType();
		AttributeValueEncoder attributeValueEncoder = getAttributeValueEncoder(type);
		attributeValueEncoder.encode(attribute, response, context);
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		ResponseWriter response = context.getResponseWriter();
		response.endElement("span");
	}

	private interface AttributeValueEncoder {
		void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException;
	}

	@SupportedType(SafeOnlineConstants.STRING_TYPE)
	public static class StringAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			String value = attribute.getStringValue();
			if (null == value) {
				ResourceBundle messages = AttributeComponentUtil
						.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				value = "[" + noValueStr + "]";
			}
			response.write(value);
		}
	}

	@SupportedType(SafeOnlineConstants.BOOLEAN_TYPE)
	public static class BooleanAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			Boolean value = attribute.getBooleanValue();
			if (null == value) {
				ResourceBundle messages = AttributeComponentUtil
						.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				response.write("[" + noValueStr + "]");
				return;
			}
			response.write(value.toString());
		}
	}

	@SupportedType(SafeOnlineConstants.BLOB_TYPE)
	public static class BlobAttributeValueEncoder implements
			AttributeValueEncoder {
		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			response.write("[BLOB]");
		}
	}

	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	private @interface SupportedType {
		String value();
	}

	private static final Map<String, Class<? extends AttributeValueEncoder>> attributeValueEncoders = new HashMap<String, Class<? extends AttributeValueEncoder>>();

	static {
		registerAttributeValueEncoder(StringAttributeValueEncoder.class);
		registerAttributeValueEncoder(BooleanAttributeValueEncoder.class);
		registerAttributeValueEncoder(BlobAttributeValueEncoder.class);
	}

	private static void registerAttributeValueEncoder(
			Class<? extends AttributeValueEncoder> clazz) {
		SupportedType supportedType = clazz.getAnnotation(SupportedType.class);
		if (null == supportedType) {
			throw new RuntimeException(
					"attribute value encoder required SupportedType meta-data annotation");
		}
		String type = supportedType.value();
		if (attributeValueEncoders.containsKey(type)) {
			throw new RuntimeException(
					"duplicate attribute value encoder entry for type: " + type);
		}
		attributeValueEncoders.put(type, clazz);
	}

	private static final Map<String, AttributeValueEncoder> instances = new HashMap<String, AttributeValueEncoder>();

	/**
	 * Gives back an instance of the requested attribute value encoder. There is
	 * a chance for a datarace here, but we don't care.
	 * 
	 * @param type
	 * @return
	 */
	private static AttributeValueEncoder getAttributeValueEncoder(String type) {
		AttributeValueEncoder attributeValueEncoder = instances.get(type);
		if (null != attributeValueEncoder) {
			return attributeValueEncoder;
		}
		Class<? extends AttributeValueEncoder> attributeValueEncoderClass = attributeValueEncoders
				.get(type);
		if (null == attributeValueEncoderClass) {
			throw new RuntimeException("unsupported type: " + type);
		}
		try {
			attributeValueEncoder = attributeValueEncoderClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(
					"could not create attribute value encoder instance for type: "
							+ type + "; " + e.getMessage(), e);
		}
		instances.put(type, attributeValueEncoder);
		return attributeValueEncoder;
	}
}
