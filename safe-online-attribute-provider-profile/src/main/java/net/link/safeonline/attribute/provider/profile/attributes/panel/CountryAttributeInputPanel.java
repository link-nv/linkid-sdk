/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider.profile.attributes.panel;

import java.util.Arrays;
import java.util.Locale;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.profile.attributes.Country;
import net.link.util.wicket.component.feedback.ErrorComponentFeedbackLabel;
import net.link.util.wicket.component.input.RequiredDropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.jetbrains.annotations.Nullable;


public abstract class CountryAttributeInputPanel extends AttributeInputPanel {

    public static final String COUNTRIES_ID          = "countries";
    public static final String COUNTRIES_FEEDBACK_ID = "countriesFeedback";

    TextField<String> valueField;

    protected CountryAttributeInputPanel(String id, final AttributeCore attribute) {

        super( id, attribute );

        RequiredDropDownChoice<Country> countriesDropDownDropDown = new RequiredDropDownChoice<Country>( COUNTRIES_ID,
                new IModel<Country>() {

                    @Nullable
                    @Override
                    public Country getObject() {

                        if (null != attribute.getValue())
                            return Country.toCountry( (String) attribute.getValue() );
                        return null;
                    }

                    @Override
                    public void setObject(final Country object) {

                        attribute.setValue( object.getValue() );
                    }

                    @Override
                    public void detach() {

                    }
                }, Arrays.asList( Country.values() ), new IChoiceRenderer<Country>() {

            @Override
            public Object getDisplayValue(Country object) {

                return localize( object, getLocale() );
            }

            @Override
            public String getIdValue(Country object, int index) {

                return object.toString();
            }
        }
        );
        countriesDropDownDropDown.setNullValid( false );
        add( countriesDropDownDropDown );
        add( new ErrorComponentFeedbackLabel( COUNTRIES_FEEDBACK_ID, countriesDropDownDropDown ) );
    }

    @Override
    public void onMissingAttribute() {

        // do nothing
    }

    protected abstract String localize(Country country, Locale locale);
}