package net.link.safeonline.attribute.provider.profile.attributes;

import java.util.Locale;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.DataType;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.profile.attributes.panel.CountryAttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;


public class CountryAttribute extends AbstractProfileAttribute {

    public CountryAttribute(String providerJndi) {

        super( providerJndi, null, DataType.STRING );
    }

    @Override
    public AttributeInputPanel findAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                       final AttributeCore attribute) {

        return new CountryAttributeInputPanel( id, attribute ) {
            @Override
            protected String localize(final Country country, final Locale locale) {

                return linkIDService.getLocalizationService().findText( "attribute.profile.country." + country.getKey(), locale );
            }
        };
    }

    public String getName() {

        return "profile.country";
    }
}
