/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider.template.panel;

import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.DataType;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;


public class CustomAttributeInputPanel extends AttributeInputPanel {

    public static final String VALUE_ID = "value";

    private static final String DELIMITER = ";";

    TextField<String> valueField;

    public CustomAttributeInputPanel(String id, final AttributeCore attribute) {
        super( id, attribute );

        if (attribute.getAttributeType().getType() != DataType.STRING) {
            throw new RuntimeException( "Only support for Strings" );
        }

        valueField = createTextField();

        valueField.setOutputMarkupId( true );
        valueField.setEnabled( attribute.getAttributeType().isUserEditable() );

        add( valueField );
    }

    @Override
    public void onMissingAttribute() {

        // do nothing
    }

    private TextField<String> createTextField() {

        return new TextField<String>( VALUE_ID, new IModel<String>() {

            @SuppressWarnings("unchecked")
            public String getObject() {
                return (String) attribute.getValue();
            }

            public void setObject(final String object) {

                if (object.isEmpty()) {
                    attribute.setValue( null );
                } else {
                    attribute.setValue( object );
                }
            }

            public void detach() {
            }
        } ) {

            @Override
            public boolean isInputNullable() {

                return true;
            }
        };
    }
}
