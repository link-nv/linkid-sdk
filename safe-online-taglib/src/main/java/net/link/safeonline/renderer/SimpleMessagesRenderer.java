package net.link.safeonline.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessages;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.faces.renderkit.html_basic.MessagesRenderer;
import com.sun.faces.util.MessageUtils;


public class SimpleMessagesRenderer extends MessagesRenderer {

    @SuppressWarnings("unchecked")
    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        Iterator messageIter = null;
        FacesMessage curMessage = null;
        ResponseWriter writer = null;

        if (context == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        if (component == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID,
                    "component"));

        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "End encoding component " + component.getId());
        }
        // suppress rendering if "rendered" property on the component is
        // false.
        if (!component.isRendered()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("End encoding component " + component.getId() + " since " + "rendered attribute is set to false ");
            }
            return;
        }
        writer = context.getResponseWriter();
        assert writer != null;

        // String clientId = ((UIMessages) component).getFor();
        String clientId = null; // FIXME? PENDING - "for" is actually gone now
        // if no clientId was included
        // if (clientId == null) {
        // and the author explicitly only wants global messages
        if (((UIMessages) component).isGlobalOnly()) {
            // make it so only global messages get displayed.
            clientId = "";
        }
        // }

        // "for" attribute optional for Messages
        messageIter = getMessageIter(context, clientId, component);

        assert messageIter != null;

        if (!messageIter.hasNext())
            return;

        boolean showSummary = ((UIMessages) component).isShowSummary();
        boolean showDetail = ((UIMessages) component).isShowDetail();
        String styleClass = (String) component.getAttributes().get("styleClass");

        while (messageIter.hasNext()) {
            curMessage = (FacesMessage) messageIter.next();

            String summary = null, detail = null;

            // make sure we have a non-null value for summary and
            // detail.
            summary = null != (summary = curMessage.getSummary())? summary: "";
            // Default to summary if we have no detail
            detail = null != (detail = curMessage.getDetail())? detail: summary;

            // Done intializing local variables. Move on to rendering.

            writer.startElement("span", component);
            if (null != styleClass) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }

            if (showSummary) {
                writer.writeText("\t", component, null);
                writer.writeText(summary, component, null);
                writer.writeText(" ", component, null);
            }
            if (showDetail) {
                writer.writeText(detail, component, null);
            }

            writer.endElement("span");

        } // messageIter

    }
}
