/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.DatatypeType;

/**
 * JSF output component for {@link AttributeDO}.
 * 
 * @author fcorneli
 * 
 */
public class AttributeOutputComponent extends UIOutput {

	public static final String ATTRIBUTE_OUTPUT_COMPONENT_TYPE = "net.link.component.attributeOutput";

	private String styleClass = "";

	public final static String STYLE_CLASS_DEFAULT = "so-output";

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter response = context.getResponseWriter();
		response.startElement("span", this);
		String clientId = getClientId(context);
		response.writeAttribute("id", clientId, "id");
		response.writeAttribute("class", STYLE_CLASS_DEFAULT + " "
				+ this.styleClass, "styleClass");
		AttributeDO attribute = (AttributeDO) getValue();
		DatatypeType type = attribute.getType();
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

	@SupportedType(DatatypeType.STRING)
	public static class StringAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			String value = attribute.getStringValue();
			if (null == value) {
				ResourceBundle messages = TaglibUtil.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				value = "[" + noValueStr + "]";
			}
			response.write(value);
		}
	}

	@SupportedType(DatatypeType.LOGIN)
	public static class LoginAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			String value = attribute.getStringValue();
			if (null == value) {
				ResourceBundle messages = TaglibUtil.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				value = "[" + noValueStr + "]";
			}
			response.write(value);
		}
	}

	@SupportedType(DatatypeType.BOOLEAN)
	public static class BooleanAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			Boolean value = attribute.getBooleanValue();
			if (null == value) {
				ResourceBundle messages = TaglibUtil.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				response.write("[" + noValueStr + "]");
				return;
			}
			response.write(value.toString());
		}
	}

	@SupportedType(DatatypeType.INTEGER)
	public static class IntegerAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			Integer value = attribute.getIntegerValue();
			if (null == value) {
				ResourceBundle messages = TaglibUtil.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				response.write("[" + noValueStr + "]");
				return;
			}
			response.write(value.toString());
		}
	}

	@SupportedType(DatatypeType.DOUBLE)
	public static class DoubleAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			Double value = attribute.getDoubleValue();
			if (null == value) {
				ResourceBundle messages = TaglibUtil.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				response.write("[" + noValueStr + "]");
				return;
			}
			response.write(value.toString());
		}
	}

	@SupportedType(DatatypeType.DATE)
	public static class DateAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(AttributeDO attribute, ResponseWriter response,
				FacesContext context) throws IOException {
			Date value = attribute.getDateValue();
			if (null == value) {
				ResourceBundle messages = TaglibUtil.getResourceBundle(context);
				String noValueStr = messages.getString("noValue");
				response.write("[" + noValueStr + "]");
				return;
			}
			response.write(value.toString());
		}
	}

	@SupportedType(DatatypeType.COMPOUNDED)
	public static class CompoundedAttributeValueEncoder implements
			AttributeValueEncoder {

		public void encode(@SuppressWarnings("unused") AttributeDO attribute,
				@SuppressWarnings("unused") ResponseWriter response,
				@SuppressWarnings("unused") FacesContext context) {
			// empty
		}
	}

	private static final Map<DatatypeType, Class<? extends AttributeValueEncoder>> attributeValueEncoders = new HashMap<DatatypeType, Class<? extends AttributeValueEncoder>>();

	static {
		/*
		 * It would be a little bit of overkill to use the JBoss Seam Scanner
		 * component over here.
		 */
		registerAttributeValueEncoder(StringAttributeValueEncoder.class);
		registerAttributeValueEncoder(LoginAttributeValueEncoder.class);
		registerAttributeValueEncoder(BooleanAttributeValueEncoder.class);
		registerAttributeValueEncoder(CompoundedAttributeValueEncoder.class);
		registerAttributeValueEncoder(IntegerAttributeValueEncoder.class);
		registerAttributeValueEncoder(DoubleAttributeValueEncoder.class);
		registerAttributeValueEncoder(DateAttributeValueEncoder.class);
	}

	private static void registerAttributeValueEncoder(
			Class<? extends AttributeValueEncoder> clazz) {
		SupportedType supportedType = clazz.getAnnotation(SupportedType.class);
		if (null == supportedType)
			throw new RuntimeException(
					"attribute value encoder requires @SupportedType meta-data annotation");
		DatatypeType type = supportedType.value();
		if (attributeValueEncoders.containsKey(type))
			throw new RuntimeException(
					"duplicate attribute value encoder entry for type: " + type);
		attributeValueEncoders.put(type, clazz);
	}

	private static final Map<DatatypeType, AttributeValueEncoder> instances = new HashMap<DatatypeType, AttributeValueEncoder>();

	/**
	 * Gives back an instance of the requested attribute value encoder. There is
	 * a chance for a datarace here, but we don't care.
	 * 
	 * @param type
	 */
	private static AttributeValueEncoder getAttributeValueEncoder(
			DatatypeType type) {
		AttributeValueEncoder attributeValueEncoder = instances.get(type);
		if (null != attributeValueEncoder)
			return attributeValueEncoder;
		Class<? extends AttributeValueEncoder> attributeValueEncoderClass = attributeValueEncoders
				.get(type);
		if (null == attributeValueEncoderClass)
			throw new RuntimeException("unsupported type: " + type);
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

	public String getStyleClass() {
		return this.styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
}
