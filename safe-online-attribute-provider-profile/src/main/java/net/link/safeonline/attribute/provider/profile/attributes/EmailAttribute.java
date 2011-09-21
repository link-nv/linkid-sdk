package net.link.safeonline.attribute.provider.profile.attributes;

import java.util.Locale;
import net.link.safeonline.attribute.provider.*;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.profile.attributes.panel.EmailAttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;


public class EmailAttribute extends AbstractProfileAttribute {

    public EmailAttribute(String providerJndi) {

        super( providerJndi, null, DataType.STRING );
    }

    public String getName() {

        return "profile.email";
    }

    @Override
    public AttributeType getAttributeType() {
        //allow multiple email addresses per user, make type multivalued
        return new AttributeType( getName(), getDataType(), getProviderJndi(), true, true, true, false, false );
    }

     @Override
    public AttributeInputPanel findAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                       final AttributeCore attribute) {

        return new EmailAttributeInputPanel( id, attribute );
    }
}
