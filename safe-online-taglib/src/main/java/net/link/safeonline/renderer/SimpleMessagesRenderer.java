package net.link.safeonline.renderer;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

public class SimpleMessagesRenderer extends Renderer {

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

		if (false == component.isRendered())
			return;

		ResponseWriter writer = context.getResponseWriter();
		assert writer != null;

		String styleClass = (String) component.getAttributes()
				.get("styleClass");

		Iterator<FacesMessage> iter = context.getMessages();

		while (iter.hasNext()) {
			writer.startElement("span", component);
			writer.writeAttribute("class", styleClass, "class");
			writer.write(iter.next().getSummary());
			writer.endElement("span");
		}
	}
}
