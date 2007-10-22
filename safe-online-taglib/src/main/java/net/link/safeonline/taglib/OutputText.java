package net.link.safeonline.taglib;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.util.HTMLEncoder;

public class OutputText extends HtmlOutputText {

	public OutputText() {
		super();
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		String text = RendererUtils.getStringValue(context, this);
		text = HTMLEncoder.encode(text, true, true);
		text = text.replaceAll("\n", "<br/>");
		text = text.replaceAll("\r", "<br/>");
		renderOutputText(context, this, text, false);
	}

	public void renderOutputText(FacesContext facesContext,
			UIComponent component, String text, @SuppressWarnings("unused")
			boolean escape) throws IOException {
		if (text != null) {
			ResponseWriter writer = facesContext.getResponseWriter();
			boolean span = false;

			if (component.getId() != null
					&& !component.getId().startsWith(
							UIViewRoot.UNIQUE_ID_PREFIX)) {
				span = true;

				writer.startElement(HTML.SPAN_ELEM, component);

				HtmlRendererUtils.writeIdIfNecessary(writer, component,
						facesContext);

				HtmlRendererUtils.renderHTMLAttributes(writer, component,
						HTML.COMMON_PASSTROUGH_ATTRIBUTES);

			} else {
				span = HtmlRendererUtils
						.renderHTMLAttributesWithOptionalStartElement(writer,
								component, HTML.SPAN_ELEM,
								HTML.COMMON_PASSTROUGH_ATTRIBUTES);
			}

			writer.writeText(text, JSFAttr.VALUE_ATTR);
			if (span) {
				writer.endElement(HTML.SPAN_ELEM);
			}
		}
	}

}
