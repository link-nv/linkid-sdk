package net.link.safeonline.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIMessage;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.HtmlBasicRenderer;
import com.sun.faces.renderkit.html_basic.OutputMessageRenderer;
import com.sun.faces.util.MessageUtils;


public class SimpleMessageRenderer extends HtmlBasicRenderer {

    private OutputMessageRenderer omRenderer = null;


    // ------------------------------------------------------------ Constructors

    public SimpleMessageRenderer() {

        omRenderer = new OutputMessageRenderer();

    }

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException {

        if (context == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        if (component == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID,
                    "component"));
        if (component instanceof UIOutput) {
            omRenderer.encodeBegin(context, component);
            return;
        }

    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component)
            throws IOException {

        if (context == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        if (component == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID,
                    "component"));
        if (component instanceof UIOutput) {
            omRenderer.encodeChildren(context, component);
            return;
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException {

        Iterator messageIter = null;
        FacesMessage curMessage = null;
        ResponseWriter writer = null;

        if (context == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "context"));
        if (component == null)
            throw new NullPointerException(MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID,
                    "component"));

        if (component instanceof UIOutput) {
            omRenderer.encodeEnd(context, component);
            return;
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Begin encoding component " + component.getId());
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

        String clientId = ((UIMessage) component).getFor();
        // "for" attribute required for Message. Should be taken care of
        // by TLD in JSP case, but need to cover non-JSP case.
        if (clientId == null) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("'for' attribute cannot be null");
            }
            return;
        }

        messageIter = getMessageIter(context, clientId, component);

        assert messageIter != null;
        if (!messageIter.hasNext())
            // no messages to render
            return;
        curMessage = (FacesMessage) messageIter.next();

        String summary = null, detail = null, severityStyle = null, severityStyleClass = null;
        boolean showSummary = ((UIMessage) component).isShowSummary(), showDetail = ((UIMessage) component).isShowDetail();

        // make sure we have a non-null value for summary and
        // detail.
        summary = null != (summary = curMessage.getSummary())? summary: "";
        // Default to summary if we have no detail
        detail = null != (detail = curMessage.getDetail())? detail: summary;

        if (curMessage.getSeverity() == FacesMessage.SEVERITY_INFO) {
            severityStyle = (String) component.getAttributes().get("infoStyle");
            severityStyleClass = (String) component.getAttributes().get("infoClass");
        } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_WARN) {
            severityStyle = (String) component.getAttributes().get("warnStyle");
            severityStyleClass = (String) component.getAttributes().get("warnClass");
        } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_ERROR) {
            severityStyle = (String) component.getAttributes().get("errorStyle");
            severityStyleClass = (String) component.getAttributes().get("errorClass");
        } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_FATAL) {
            severityStyle = (String) component.getAttributes().get("fatalStyle");
            severityStyleClass = (String) component.getAttributes().get("fatalClass");
        }

        String style = (String) component.getAttributes().get("style");
        String styleClass = (String) component.getAttributes().get("styleClass");
        String dir = (String) component.getAttributes().get("dir");
        String lang = (String) component.getAttributes().get("lang");
        String title = (String) component.getAttributes().get("title");

        // if we have style and severityStyle
        if (style != null && severityStyle != null) {
            // severityStyle wins
            style = severityStyle;
        }
        // if we have no style, but do have severityStyle
        else if (style == null && severityStyle != null) {
            // severityStyle wins
            style = severityStyle;
        }

        // if we have styleClass and severityStyleClass
        if (styleClass != null && severityStyleClass != null) {
            // severityStyleClass wins
            styleClass = severityStyleClass;
        }
        // if we have no styleClass, but do have severityStyleClass
        else if (styleClass == null && severityStyleClass != null) {
            // severityStyleClass wins
            styleClass = severityStyleClass;
        }

        // Done intializing local variables. Move on to rendering.

        writer.startElement("span", component);

        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }

        writeIdAttributeIfNecessary(context, writer, component);

        if (dir != null) {
            writer.writeAttribute("dir", dir, "dir");
        }
        if (lang != null) {
            writer.writeAttribute(RenderKitUtils.prefixAttribute("lang", writer), lang, "lang");
        }
        if (title != null) {
            writer.writeAttribute("title", title, "title");
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

        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "End encoding component " + component.getId());
        }

    }

}
