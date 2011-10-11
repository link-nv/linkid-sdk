package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.*;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.input.DefaultAttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;


public class EmailConfirmedAttribute extends AbstractProfileAttribute {

    public static String NAME = "profile.email.confirmed";

    public EmailConfirmedAttribute(String providerJndi) {

        super( providerJndi, null, DataType.STRING );
    }

    public String getName() {

        return NAME;
    }

    @Override
    public AttributeType getAttributeType() {

        return new AttributeType( getName(), getDataType(), getProviderJndi(), false, true, true, false, false );
    }

     @Override
    public AttributeInputPanel findAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                       final AttributeCore attribute) {

        return new DefaultAttributeInputPanel( id, attribute );
    }
}
