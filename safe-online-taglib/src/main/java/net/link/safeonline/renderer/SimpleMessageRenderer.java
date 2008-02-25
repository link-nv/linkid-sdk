package net.link.safeonline.renderer;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleMessageRenderer extends Renderer {

	private static final Log LOG = LogFactory
			.getLog(SimpleMessageRenderer.class);

	@Override
	public void encodeEnd(FacesContext context, UIComponent component)
			throws IOException {

		if (false == component.isRendered())
			return;

		ResponseWriter writer = context.getResponseWriter();
		assert writer != null;

		String styleClass = (String) component.getAttributes()
				.get("styleClass");

		String clientId = ((UIMessage) component).getFor();
		if (null == clientId)
			return;

		clientId = augmentIdReference(context, clientId, component);

		LOG.debug("rendering messages for: " + clientId);

		Iterator<FacesMessage> iter = context.getMessages(clientId);

		int count = 0;
		while (iter.hasNext()) {
			writer.startElement("span", component);
			writer.writeAttribute("class", styleClass, "class");
			writer.write(iter.next().getSummary());
			writer.endElement("span");
			count++;
		}

		LOG.debug("rendered " + count + " messages");
	}

	private String augmentIdReference(@SuppressWarnings("unused") FacesContext context, String forValue,
			UIComponent fromComponent) {

		int forSuffix = forValue.lastIndexOf(UIViewRoot.UNIQUE_ID_PREFIX);
		if (forSuffix <= 0) {
			String id = fromComponent.getId();
			int idSuffix = id.lastIndexOf(UIViewRoot.UNIQUE_ID_PREFIX);
			if (idSuffix > 0) {
				forValue += id.substring(idSuffix);
			}
		}
		return forValue;

	}

}
