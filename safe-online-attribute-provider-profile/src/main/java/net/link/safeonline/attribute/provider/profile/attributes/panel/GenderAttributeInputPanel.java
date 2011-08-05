/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider.profile.attributes.panel;

import java.util.Locale;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.util.wicket.component.feedback.ErrorComponentFeedbackLabel;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;


public abstract class GenderAttributeInputPanel extends AttributeInputPanel {

    public static final String VALUE_GROUP_ID    = "valueGroup";
    public static final String MALE_ID           = "male";
    public static final String MALE_RADIO_ID     = "maleRadio";
    public static final String FEMALE_ID         = "female";
    public static final String FEMALE_RADIO_ID   = "femaleRadio";
    public static final String VALUE_FEEDBACK_ID = "valueFeedback";

    TextField<String> valueField;

    protected GenderAttributeInputPanel(String id, final AttributeCore attribute) {

        super( id, attribute );

        RadioGroup<Gender> genderGroup = new RadioGroup<Gender>( VALUE_GROUP_ID, new IModel<Gender>() {

            public Gender getObject() {

                String genderString = (String) attribute.getValue();

                if (null == genderString)
                    return null;

                if (genderString.toLowerCase().startsWith( "m" ))
                    return Gender.MALE;
                else
                    return Gender.FEMALE;
            }

            public void setObject(final Gender object) {

                switch (object) {

                    case MALE:
                        attribute.setValue( "M" );
                        return;
                    case FEMALE:
                        attribute.setValue( "F" );
                }
            }

            public void detach() {

            }
        } );
        genderGroup.setRequired( true );
        genderGroup.setRenderBodyOnly( false );
        add( genderGroup );
        add( new ErrorComponentFeedbackLabel( VALUE_FEEDBACK_ID, genderGroup, new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return getRadioErrorMessage( getLocale() );
            }
        } ) );

        // male
        Radio<Gender> maleRadio = new Radio<Gender>( MALE_RADIO_ID, new AbstractReadOnlyModel<Gender>() {
            @Override
            public Gender getObject() {

                return Gender.MALE;
            }
        } );
        maleRadio.setLabel( new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return localize( Gender.MALE, getLocale() );
            }
        } );
        genderGroup.add( new SimpleFormComponentLabel( MALE_ID, maleRadio ) );
        genderGroup.add( maleRadio );

        // female
        Radio<Gender> femaleRadio = new Radio<Gender>( FEMALE_RADIO_ID, new AbstractReadOnlyModel<Gender>() {
            @Override
            public Gender getObject() {

                return Gender.FEMALE;
            }
        } );
        femaleRadio.setLabel( new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {

                return localize( Gender.FEMALE, getLocale() );
            }
        } );
        genderGroup.add( new SimpleFormComponentLabel( FEMALE_ID, femaleRadio ) );
        genderGroup.add( femaleRadio );
    }

    @Override
    public void onMissingAttribute() {

        // do nothing
    }

    protected abstract String localize(Gender gender, Locale locale);

    protected abstract String getRadioErrorMessage(Locale locale);

    public enum Gender {
        MALE,
        FEMALE
    }
}
