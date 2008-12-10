/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.converter;

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import net.link.safeonline.custom.converter.PhoneNumber;
import net.link.safeonline.taglib.TaglibUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * PhoneNumber converter
 * 
 * @author wvdhaute
 * 
 */
public class PhoneNumberConverter implements Converter {

    private static final Log LOG = LogFactory.getLog(PhoneNumberConverter.class);


    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {

        if (value == null)
            return null;

        LOG.debug("convert " + value);

        String convertedNumber = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(value);
        if (null == convertedNumber) {
            ResourceBundle messages = TaglibUtil.getResourceBundle(facesContext);
            FacesMessage facesMessage = new FacesMessage(messages.getString("invalidPhoneNumber"));
            throw new ConverterException(facesMessage);
        }

        return new PhoneNumber(convertedNumber);
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {

        return value.toString();
    }

}
