/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.converter;

import java.util.Locale;

import net.link.safeonline.custom.converter.PhoneNumber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;


/**
 * <h2>{@link PhoneNumberConverter}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 2, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class PhoneNumberConverter implements IConverter {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(PhoneNumberConverter.class);


    /**
     * {@inheritDoc}
     */
    public Object convertToObject(String value, Locale locale) {

        if (value == null)
            return null;

        LOG.debug("convert " + value);

        String convertedNumber = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(value);
        if (null == convertedNumber)
            throw new ConversionException("Illegal phonenumber format");

        return new PhoneNumber(convertedNumber);
    }

    /**
     * {@inheritDoc}
     */
    public String convertToString(Object value, Locale locale) {

        LOG.debug("convert " + value + " to String");
        PhoneNumber number = (PhoneNumber) value;

        return value != null? number.getNumber(): null;
    }

}
