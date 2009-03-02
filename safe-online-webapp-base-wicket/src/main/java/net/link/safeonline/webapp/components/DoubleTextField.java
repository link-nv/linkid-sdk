/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DoubleConverter;


/**
 * <h2>{@link DoubleTextField}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 27, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DoubleTextField extends TextField<Double> implements ITextFormatProvider {

    private static final long serialVersionUID = 1L;

    /**
     * The converter for the TextField
     */
    private IConverter        converter        = null;


    /**
     * Creates a new DoubleTextField
     * 
     * @param id
     *            The id of the text field
     * 
     * @see org.apache.wicket.markup.html.form.TextField
     */
    public DoubleTextField(String id) {

        this(id, null);
    }

    /**
     * Creates a new DoubleTextField
     * 
     * @param id
     *            The id of the text field
     * @param model
     *            The model
     * 
     * @see org.apache.wicket.markup.html.form.TextField
     */
    public DoubleTextField(String id, IModel<Double> model) {

        super(id, model, Double.class);
        converter = new DoubleConverter();
    }

    /**
     * Returns the default converter.
     * 
     * @param type
     *            The type for which the convertor should work
     * 
     * @return A pattern-specific converter
     * 
     * @see org.apache.wicket.markup.html.form.TextField
     */
    @Override
    public IConverter getConverter(Class<?> type) {

        if (converter == null)
            return super.getConverter(type);
        return converter;
    }

    /**
     * Returns the text format pattern.
     * 
     * @see org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider#getTextFormat()
     */
    public String getTextFormat() {

        return null;
    }
}
