package net.link.safeonline.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.model.SelectItem;
import javax.faces.render.Renderer;

/**
 * This renderer is based on Sun's RI for the radio button renderer
 *
 * @author dhouthoo
 *
 */
public class NoTablesRadioButtonRenderer extends Renderer {

	@Override
	public void decode(FacesContext context, UIComponent component) {

		String clientId = component.getClientId(context);
		assert clientId != null;

		Map<String, String> requestParameterMap = context.getExternalContext()
				.getRequestParameterMap();
		if (requestParameterMap.containsKey(clientId)) {
			String newValue = requestParameterMap.get(clientId);
			setSubmittedValue(component, newValue);
		} else
			setSubmittedValue(component, "");

		return;

	}

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

		if (false == component.isRendered())
			return;

		ResponseWriter writer = context.getResponseWriter();
		assert writer != null;

		String styleClass = (String) component.getAttributes()
				.get("styleClass");

		Iterator<SelectItem> items = getSelectItems(context, component);
		SelectItem curItem = null;
		int idx = -1;
		while (items.hasNext()) {
			curItem = items.next();
			idx++;
			renderOption(context, component, curItem, idx, styleClass);

		}
	}

	protected void renderOption(FacesContext context, UIComponent component,
			SelectItem curItem, int itemNumber, String styleClass)
			throws IOException {

		ResponseWriter writer = context.getResponseWriter();
		assert writer != null;

		UISelectOne selectOne = (UISelectOne) component;
		Object curValue = selectOne.getSubmittedValue();
		if (curValue == null)
			curValue = selectOne.getValue();

		Class<?> type = String.class;
		if (curValue != null)
			type = curValue.getClass();
		Object itemValue = curItem.getValue();
		// Map<String, Object> requestMap = context.getExternalContext()
		// .getRequestMap();
		// requestMap.put(
		// ConverterPropertyEditorBase.TARGET_COMPONENT_ATTRIBUTE_NAME,
		// component);
		Object newValue = context.getApplication().getExpressionFactory()
				.coerceToType(itemValue, type);

		String idString = component.getClientId(context)
				+ NamingContainer.SEPARATOR_CHAR + Integer.toString(itemNumber);

		writer.startElement("label", component);
		writer.writeAttribute("for", idString, "for");

		String itemLabel = curItem.getLabel();
		if (itemLabel != null) {
			writer.writeText(" ", component, null);
			if (!curItem.isEscape())
				// It seems the ResponseWriter API should
				// have a writeText() with a boolean property
				// to determine if it content written should
				// be escaped or not.
				writer.write(itemLabel);
			else
				writer.writeText(itemLabel, component, "label");
		}
		writer.endElement("label");

		writer.startElement("input", component);
		writer.writeAttribute("type", "radio", "type");
		if (styleClass != null)
			writer.writeAttribute("class", styleClass, "class");
		if (newValue.equals(curValue))
			writer.writeAttribute("checked", Boolean.TRUE, null);
		writer.writeAttribute("name", component.getClientId(context),
				"clientId");

		writer.writeAttribute("id", idString, "id");

		writer.writeAttribute("value", curItem.getValue().toString(), "value");

		writer.endElement("input");

	}

	public static Iterator<SelectItem> getSelectItems(
			@SuppressWarnings("unused")
			FacesContext context, UIComponent component) {

		ArrayList<SelectItem> list = new ArrayList<SelectItem>();
		for (UIComponent kid : component.getChildren())
			if (kid instanceof UISelectItem) {
				UISelectItem item = (UISelectItem) kid;
				Object value = item.getValue();

				if (value == null)
					list.add(new SelectItem(item.getItemValue(), item
							.getItemLabel(), item.getItemDescription(), item
							.isItemDisabled(), item.isItemEscaped()));
				else if (value instanceof SelectItem)
					list.add((SelectItem) value);
				else {
					// empty
				}
			} else if (kid instanceof UISelectItems) {
				Object value = ((UISelectItems) kid).getValue();
				if (value instanceof SelectItem)
					list.add((SelectItem) value);
				else if (value instanceof SelectItem[]) {
					SelectItem[] items = (SelectItem[]) value;
					for (SelectItem item : items)
						list.add(item);
				} else if (value instanceof Collection) {
					for (Object element : (Collection<?>) value)
						if (SelectItem.class.isInstance(element))
							list.add((SelectItem) element);
						else {
							// empty
						}
				} else if (value instanceof Map) {
					Map<?, ?> optionMap = (Map<?, ?>) value;
					for (Object o : optionMap.entrySet()) {
						Entry<?, ?> entry = (Entry<?, ?>) o;
						Object key = entry.getKey();
						Object val = entry.getValue();
						if (key == null || val == null)
							continue;
						list.add(new SelectItem(val, key.toString()));
					}
				} else {
					// empty
				}
			}
		return list.iterator();

	}

	public void setSubmittedValue(UIComponent component, Object value) {
		if (component instanceof UIInput)
			((UIInput) component).setSubmittedValue(value);

	}

}
