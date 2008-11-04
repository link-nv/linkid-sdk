package net.link.safeonline.taglib;

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class EqualValidator implements Validator {

    private static final Log LOG = LogFactory.getLog(EqualValidator.class);


    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        String confirm = (String) value;

        String textId = (String) component.getAttributes().get("for");

        String message = (String) component.getAttributes().get("message");

        UIInput input = (UIInput) context.getViewRoot().findComponent(textId);

        String text = (String) input.getValue();

        if (text == null || confirm == null)
            return;
        else if (text.equals(confirm))
            return;

        FacesMessage facesMessage;
        if (null == message) {
            ResourceBundle messages = TaglibUtil.getResourceBundle(context);
            facesMessage = new FacesMessage(messages.getString("notEqual"));
        } else {
            facesMessage = new FacesMessage(message);
        }
        LOG.error("fields are not equal");
        throw new ValidatorException(facesMessage);
    }

}
