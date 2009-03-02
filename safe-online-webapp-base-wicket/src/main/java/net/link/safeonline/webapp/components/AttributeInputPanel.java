/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components;

import java.util.Date;

import net.link.safeonline.data.AttributeDO;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link AttributeInputPanel}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 26, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AttributeInputPanel extends Panel {

    private static final long  serialVersionUID    = 1L;

    public static final String NAME_LABEL_ID       = "name";

    public static final String STRING_ID           = "string";
    public static final String STRING_FEEDBACK_ID  = "string_feedback";

    public static final String DOUBLE_ID           = "double";
    public static final String DOUBLE_FEEDBACK_ID  = "double_feedback";

    public static final String INTEGER_ID          = "integer";
    public static final String INTEGER_FEEDBACK_ID = "integer_feedback";

    public static final String DATE_ID             = "date";
    public static final String DATE_FEEDBACK_ID    = "date_feedback";

    public static final String BOOLEAN_ID          = "boolean";
    public static final String BOOLEAN_FEEDBACK_ID = "boolean_feedback";

    AttributeDO                attribute;


    public AttributeInputPanel(String id, final AttributeDO attribute, boolean required) {

        super(id);

        this.attribute = attribute;

        String name = attribute.getHumanReadableName();
        if (null == name) {
            name = attribute.getName();
        }
        add(new Label(NAME_LABEL_ID, name));

        TextField<String> stringField = new TextField<String>(STRING_ID, getStringModel());
        stringField.setVisible(false);
        stringField.setRequired(required);
        stringField.setEnabled(attribute.isEditable());
        add(stringField);
        add(new ErrorComponentFeedbackLabel(STRING_FEEDBACK_ID, stringField, new Model<String>(
                getLocalizer().getString("enterAValue", this))));

        DoubleTextField doubleField = new DoubleTextField(DOUBLE_ID, getDoubleModel());
        doubleField.setVisible(false);
        doubleField.setRequired(required);
        doubleField.setEnabled(attribute.isEditable());
        add(doubleField);
        add(new ErrorComponentFeedbackLabel(DOUBLE_FEEDBACK_ID, doubleField, new Model<String>(
                getLocalizer().getString("enterAValue", this))));

        IntegerTextField integerField = new IntegerTextField(INTEGER_ID, getIntegerModel());
        integerField.setVisible(false);
        integerField.setRequired(required);
        integerField.setEnabled(attribute.isEditable());
        add(integerField);
        add(new ErrorComponentFeedbackLabel(INTEGER_FEEDBACK_ID, integerField, new Model<String>(getLocalizer().getString("enterAValue",
                this))));

        DateTextField dateField = new DateTextField(DATE_ID, getDateModel(), "dd/MM/yyyy");
        dateField.add(new DatePicker());
        dateField.setVisible(false);
        dateField.setRequired(required);
        dateField.setEnabled(attribute.isEditable());
        add(dateField);
        add(new ErrorComponentFeedbackLabel(DATE_FEEDBACK_ID, dateField, new Model<String>(getLocalizer().getString("enterAValue", this))));

        CheckBox booleanField = new CheckBox(BOOLEAN_ID, getBooleanModel());
        booleanField.setVisible(false);
        booleanField.setRequired(required);
        booleanField.setEnabled(attribute.isEditable());
        add(booleanField);
        add(new ErrorComponentFeedbackLabel(BOOLEAN_FEEDBACK_ID, booleanField, new Model<String>(getLocalizer().getString("enterAValue",
                this))));

        switch (attribute.getType()) {
            case STRING: {
                stringField.setVisible(true);
                break;
            }
            case DOUBLE: {
                doubleField.setVisible(true);
                break;
            }
            case INTEGER: {
                integerField.setVisible(true);
                break;
            }
            case DATE: {
                dateField.setVisible(true);
                break;
            }
            case BOOLEAN: {
                booleanField.setVisible(true);
                break;
            }
            case COMPOUNDED:
        }

    }

    public Model<String> getStringModel() {

        return new Model<String>() {

            private static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            @Override
            public String getObject() {

                return attribute.getStringValue();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void setObject(String object) {

                attribute.setStringValue(object);
            }
        };
    }

    public Model<Double> getDoubleModel() {

        return new Model<Double>() {

            private static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            @Override
            public Double getObject() {

                return attribute.getDoubleValue();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void setObject(Double object) {

                attribute.setDoubleValue(object);
            }

        };
    }

    public Model<Integer> getIntegerModel() {

        return new Model<Integer>() {

            private static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            @Override
            public Integer getObject() {

                return attribute.getIntegerValue();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void setObject(Integer object) {

                attribute.setIntegerValue(object);
            }
        };
    }

    public Model<Date> getDateModel() {

        return new Model<Date>() {

            private static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            @Override
            public Date getObject() {

                return attribute.getDateValue();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void setObject(Date object) {

                attribute.setDateValue(object);
            }
        };
    }

    public Model<Boolean> getBooleanModel() {

        return new Model<Boolean>() {

            private static final long serialVersionUID = 1L;


            /**
             * {@inheritDoc}
             */
            @Override
            public Boolean getObject() {

                return attribute.getBooleanValue();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void setObject(Boolean object) {

                attribute.setBooleanValue(object);
            }
        };
    }

}
