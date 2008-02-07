package net.link.safeonline.renderer;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleMessagesRenderer extends Renderer {

	private static final Log LOG = LogFactory
			.getLog(SimpleMessagesRenderer.class);

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

		if (false == component.isRendered())
			return;

		ResponseWriter writer = context.getResponseWriter();
		assert writer != null;

		String styleClass = (String) component.getAttributes()
				.get("styleClass");

		Boolean globalOnly = (Boolean) component.getAttributes().get(
				"globalOnly");

		Iterator<FacesMessage> iter;
		if (null != globalOnly && true == globalOnly.booleanValue()) {
			iter = context.getMessages(null);
		} else {
			iter = context.getMessages();
		}

		LOG.debug("rendering messages");

		int count = 0;
		while (iter.hasNext()) {
			FacesMessage message = iter.next();
			writer.startElement("span", component);
			writer.writeAttribute("class", styleClass, "class");
			writer.write(message.getSummary());
			writer.endElement("span");
			count++;
		}

		LOG.debug("rendered " + count + " messages");
	}
}
