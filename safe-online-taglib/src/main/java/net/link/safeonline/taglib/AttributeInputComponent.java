/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.validator.Validator;

import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.entity.DatatypeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;

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
		Validator attributeValidator = new AttributeValidator();
		addValidator(attributeValidator);
	}

	@Override
	public void decode(FacesContext context) {
		LOG.debug("decode");

		AttributeDO attribute = (AttributeDO) getValue();
		DatatypeType type = attribute.getType();
		Renderer renderer = renderers.get(type);
		if (null != renderer) {
			renderer.decode(context, this);
		}
		super.decode(context);
	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		LOG.debug("encodeBegin");
		if (false == isRendered()) {
			return;
		}

		String clientId = getClientId(context);
		ResponseWriter responseWriter = context.getResponseWriter();
		responseWriter.startElement("span", this);
		responseWriter.writeAttribute("id", clientId, "id");

		AttributeDO attribute = (AttributeDO) getValue();
		DatatypeType type = attribute.getType();
		Renderer renderer = renderers.get(type);
		if (null == renderer) {
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
		DatatypeType type = attribute.getType();
		Renderer renderer = renderers.get(type);
		if (null != renderer) {
			renderer.encodeEnd(context, this);
		}

		ResponseWriter responseWriter = context.getResponseWriter();
		responseWriter.endElement("span");
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

	private static final Map<DatatypeType, Renderer> renderers;

	static {
		renderers = new HashMap<DatatypeType, Renderer>();
		registerRenderer(StringRenderer.class);
		registerRenderer(BooleanRenderer.class);
		registerRenderer(CompoundedRenderer.class);
		registerRenderer(IntegerRenderer.class);
		registerRenderer(DoubleRenderer.class);
		registerRenderer(DateRenderer.class);
	}

	private static void registerRenderer(Class<? extends Renderer> clazz) {
		SupportedType supportedType = clazz.getAnnotation(SupportedType.class);
		if (null == supportedType) {
			throw new RuntimeException(
					"renderer requires SupportedType meta-data annotation");
		}
		DatatypeType type = supportedType.value();
		if (renderers.containsKey(type)) {
			throw new RuntimeException("duplicate renderer entry for type: "
					+ type);
		}
		try {
			renderers.put(type, clazz.newInstance());
		} catch (Exception e) {
			throw new RuntimeException("instantiation error for class: "
					+ clazz.getName());
		}
	}

	@SupportedType(DatatypeType.COMPOUNDED)
	public static class CompoundedRenderer implements Renderer {

		public void decode(FacesContext context, UIInput inputComponent) {
		}

		public void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException {
		}

		public void encodeEnd(FacesContext context, UIInput inputComponent)
				throws IOException {
		}
	}

	@SupportedType(DatatypeType.STRING)
	public static class StringRenderer implements Renderer {

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

			if (false == attribute.isEditable()) {
				LOG.debug("setting disabled to true");
				responseWriter.writeAttribute("disabled", "true", null);
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

			LOG.debug("decoded value: " + decodedValue);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			AttributeDO newAttribute = attribute.clone();

			newAttribute.setStringValue(decodedValue);

			inputComponent.setSubmittedValue(newAttribute);
		}
	}

	@SupportedType(DatatypeType.INTEGER)
	public static class IntegerRenderer implements Renderer {

		public void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException {
			String clientId = inputComponent.getClientId(context);
			ResponseWriter responseWriter = context.getResponseWriter();

			responseWriter.startElement("input", inputComponent);
			responseWriter.writeAttribute("id", clientId, "id");
			responseWriter.writeAttribute("type", "text", null);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			Integer value = attribute.getIntegerValue();
			String encodedValue;
			if (null == value) {
				encodedValue = "";
			} else {
				encodedValue = value.toString();
			}

			responseWriter.writeAttribute("name", clientId, null);
			responseWriter.writeAttribute("value", encodedValue, null);

			if (false == attribute.isEditable()) {
				responseWriter.writeAttribute("disabled", "true", null);
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
			String encodedValue = requestParameterMap.get(clientId);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			AttributeDO newAttribute = attribute.clone();

			try {
				Integer decodedValue;
				if (encodedValue.length() == 0) {
					decodedValue = null;
				} else {
					decodedValue = Integer.parseInt(encodedValue);
				}
				LOG.debug("decoded value: " + decodedValue);
				newAttribute.setIntegerValue(decodedValue);
			} catch (NumberFormatException e) {
				LOG.warn("not an integer: " + encodedValue);
			}

			inputComponent.setSubmittedValue(newAttribute);
		}
	}

	@SupportedType(DatatypeType.DOUBLE)
	public static class DoubleRenderer implements Renderer {

		public void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException {
			String clientId = inputComponent.getClientId(context);
			ResponseWriter responseWriter = context.getResponseWriter();

			responseWriter.startElement("input", inputComponent);
			responseWriter.writeAttribute("id", clientId, "id");
			responseWriter.writeAttribute("type", "text", null);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			Double value = attribute.getDoubleValue();
			String encodedValue;
			if (null == value) {
				encodedValue = "";
			} else {
				encodedValue = value.toString();
			}

			responseWriter.writeAttribute("name", clientId, null);
			responseWriter.writeAttribute("value", encodedValue, null);

			if (false == attribute.isEditable()) {
				responseWriter.writeAttribute("disabled", "true", null);
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
			String encodedValue = requestParameterMap.get(clientId);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			AttributeDO newAttribute = attribute.clone();

			try {
				Double decodedValue;
				if (encodedValue.length() == 0) {
					decodedValue = null;
				} else {
					decodedValue = Double.parseDouble(encodedValue);
				}
				newAttribute.setDoubleValue(decodedValue);
			} catch (NumberFormatException e) {
				LOG.warn("not a double: " + encodedValue);
			}

			inputComponent.setSubmittedValue(newAttribute);
		}

	}

	@SupportedType(DatatypeType.DATE)
	public static class DateRenderer implements Renderer {

		public void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException {
			String clientId = inputComponent.getClientId(context);
			ResponseWriter responseWriter = context.getResponseWriter();

			responseWriter.startElement("span", inputComponent);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			Date value = attribute.getDateValue();
			Calendar calendar = Calendar.getInstance();
			if (null != value) {
				calendar.setTime(value);
			}

			// day
			responseWriter.startElement("select", null);
			{
				String dayId = getDayId(clientId);
				responseWriter.writeAttribute("name", dayId, null);
				for (int dayIdx = 1; dayIdx < 32; dayIdx++) {
					responseWriter.startElement("option", null);
					responseWriter.writeAttribute("value", Integer
							.toString(dayIdx), null);
					if (calendar.get(Calendar.DAY_OF_MONTH) == dayIdx) {
						responseWriter.writeAttribute("selected", "true", null);
					}
					responseWriter.write(Integer.toString(dayIdx));
					responseWriter.endElement("option");
				}
			}
			responseWriter.endElement("select");

			// month
			responseWriter.startElement("select", null);
			{
				String monthId = getMonthId(clientId);
				responseWriter.writeAttribute("name", monthId, null);
				int month = calendar.get(Calendar.MONTH) + 1;
				for (int monthIdx = 1; monthIdx < 12; monthIdx++) {
					responseWriter.startElement("option", null);
					responseWriter.writeAttribute("value", Integer
							.toString(monthIdx), null);
					if (month == monthIdx) {
						responseWriter.writeAttribute("selected", "true", null);
					}
					responseWriter.write(Integer.toString(monthIdx));
					responseWriter.endElement("option");
				}
			}
			responseWriter.endElement("select");

			// year
			responseWriter.startElement("select", null);
			{
				String yearId = getYearId(clientId);
				responseWriter.writeAttribute("name", yearId, null);
				int year = calendar.get(Calendar.YEAR);
				for (int yearIdx = 1940; yearIdx < 2038; yearIdx++) {
					responseWriter.startElement("option", null);
					responseWriter.writeAttribute("value", Integer
							.toString(yearIdx), null);
					if (year == yearIdx) {
						responseWriter.writeAttribute("selected", "true", null);
					}
					responseWriter.write(Integer.toString(yearIdx));
					responseWriter.endElement("option");
				}
			}
			responseWriter.endElement("select");
		}

		private String getDayId(String clientId) {
			return clientId + ".day";
		}

		private String getMonthId(String clientId) {
			return clientId + ".month";
		}

		private String getYearId(String clientId) {
			return clientId + ".year";
		}

		public void encodeEnd(FacesContext context, UIInput inputComponent)
				throws IOException {
			ResponseWriter responseWriter = context.getResponseWriter();
			responseWriter.endElement("span");
		}

		@SuppressWarnings("unchecked")
		public void decode(FacesContext context, UIInput inputComponent) {
			String clientId = inputComponent.getClientId(context);
			ExternalContext externalContext = context.getExternalContext();
			Map<String, String> requestParameterMap = externalContext
					.getRequestParameterMap();
			String dayId = getDayId(clientId);
			String encodedDayValue = requestParameterMap.get(dayId);
			String monthId = getMonthId(clientId);
			String encodedMonthValue = requestParameterMap.get(monthId);
			String yearId = getYearId(clientId);
			String encodedYearValue = requestParameterMap.get(yearId);

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			AttributeDO newAttribute = attribute.clone();

			Calendar calendar = Calendar.getInstance();
			Date oldDate = newAttribute.getDateValue();
			if (null != oldDate) {
				calendar.setTime(oldDate);
			}
			calendar.set(Calendar.DAY_OF_MONTH, Integer
					.parseInt(encodedDayValue));
			calendar.set(Calendar.MONTH,
					Integer.parseInt(encodedMonthValue) - 1);
			calendar.set(Calendar.YEAR, Integer.parseInt(encodedYearValue));
			newAttribute.setDateValue(calendar.getTime());

			inputComponent.setSubmittedValue(newAttribute);
		}
	}

	@SupportedType(DatatypeType.BOOLEAN)
	public static class BooleanRenderer implements Renderer {

		private static final String INLINE_SCRIPT_ADDED = BooleanRenderer.class
				+ ".inline_script_added";

		public void encodeBegin(FacesContext context, UIInput inputComponent)
				throws IOException {
			String clientId = inputComponent.getClientId(context);
			ResponseWriter responseWriter = context.getResponseWriter();

			ResourceBundle messages = AttributeComponentUtil
					.getResourceBundle(context);

			String trueId = clientId + NamingContainer.SEPARATOR_CHAR + "true";
			String falseId = clientId + NamingContainer.SEPARATOR_CHAR
					+ "false";

			AttributeDO attribute = (AttributeDO) inputComponent.getValue();
			Boolean value = attribute.getBooleanValue();

			/*
			 * True input checkbox
			 */
			responseWriter.startElement("span", null);
			{
				responseWriter.startElement("input", null);
				{
					responseWriter.writeAttribute("type", "checkbox", null);
					responseWriter.writeAttribute("name", clientId, "id");
					responseWriter.writeAttribute("value", "true", null);
					responseWriter.writeAttribute("id", trueId, null);
					responseWriter.writeAttribute("onclick",
							"threeValuedCheckboxClicked(this.checked, this.form, '"
									+ falseId + "')", null);
					if (Boolean.TRUE.equals(value)) {
						responseWriter.writeAttribute("checked", Boolean.TRUE,
								null);
					}
					if (false == attribute.isEditable()) {
						responseWriter.writeAttribute("disabled", "true", null);
					}
					LOG.debug("message: " + messages.getString("true"));
				}
				responseWriter.endElement("input");
				responseWriter.write(messages.getString("true"));
			}
			responseWriter.endElement("span");

			/*
			 * False input checkbox
			 */
			responseWriter.startElement("span", null);
			{
				responseWriter.startElement("input", null);
				{
					responseWriter.writeAttribute("type", "checkbox", null);
					responseWriter.writeAttribute("name", clientId, "id");
					responseWriter.writeAttribute("value", "false", null);
					responseWriter.writeAttribute("id", falseId, null);
					responseWriter.writeAttribute("onclick",
							"threeValuedCheckboxClicked(this.checked, this.form, '"
									+ trueId + "')", null);
					if (Boolean.FALSE.equals(value)) {
						LOG.debug("adding checked attribute");
						responseWriter.writeAttribute("checked", Boolean.TRUE,
								null);
					}
					if (false == attribute.isEditable()) {
						LOG.debug("setting disabled to true");
						responseWriter.writeAttribute("disabled", "true", null);
					}
				}
				responseWriter.endElement("input");
				responseWriter.write(messages.getString("false"));
			}
			responseWriter.endElement("span");

			/*
			 * Javascript via tomahawk extensions filter.
			 */
			if (needToAddInlineScript(context)) {
				AddResource addResource = AddResourceFactory
						.getInstance(context);
				LOG.debug("addResource class: "
						+ addResource.getClass().getName());
				String inlineScript = getInlineScript();
				addResource.addInlineScriptAtPosition(context,
						AddResource.HEADER_BEGIN, inlineScript);
			}
		}

		private String getInlineScript() {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer
					.append("    function threeValuedCheckboxClicked(checked, form, checkboxId) {\n");
			stringBuffer.append("        if (true == checked) {\n");
			stringBuffer
					.append("            form.elements[checkboxId].checked = false;\n");
			stringBuffer.append("         }\n");
			stringBuffer.append("    }\n");
			return stringBuffer.toString();
		}

		@SuppressWarnings("unchecked")
		private boolean needToAddInlineScript(FacesContext context) {
			ExternalContext externalContext = context.getExternalContext();
			Map<String, Object> requestMap = externalContext.getRequestMap();
			if (true == requestMap.containsKey(INLINE_SCRIPT_ADDED)) {
				/*
				 * The inline script was already added by a previous call to
				 * render a component instance of this component type.
				 */
				return false;
			}
			requestMap.put(INLINE_SCRIPT_ADDED, Boolean.TRUE);
			return true;
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

			Boolean value;
			if (null == decodedValue) {
				value = null;
			} else {
				value = Boolean.parseBoolean(decodedValue);
			}
			newAttribute.setBooleanValue(value);

			inputComponent.setSubmittedValue(newAttribute);
		}
	}
}
