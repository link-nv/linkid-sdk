package net.link.safeonline.attribute.provider.profile.attributes;

import java.util.Locale;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.DataType;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.profile.attributes.panel.GenderAttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;


public class GenderAttribute extends AbstractProfileAttribute {

    public GenderAttribute(String providerJndi) {

        super( providerJndi, "device.beid.gender", DataType.STRING );
    }

    @Override
    public AttributeInputPanel findAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                       final AttributeCore attribute) {

        return new GenderAttributeInputPanel( id, attribute ) {

            @Override
            protected String localize(final Gender gender, final Locale locale) {

                switch (gender) {

                    case MALE:
                        return linkIDService.getLocalizationService().findText( "attribute.profile.gender.male", locale );
                    case FEMALE:
                        return linkIDService.getLocalizationService().findText( "attribute.profile.gender.female", locale );
                }

                return "";
            }

            @Override
            protected String getRadioErrorMessage(final Locale locale) {

                return linkIDService.getLocalizationService().findText( "attribute.profile.gender.error.select", locale );
            }
        };
    }

    public String getName() {

        return "profile.gender";
    }
}
