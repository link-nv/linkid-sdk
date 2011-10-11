package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.*;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.input.DefaultAttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;


/**
 * Expire date for emails. If they have not been confirmed before this time, other people can take over the email address.
 * Stores date the string value, because DataType.DATE has insufficient precision (it's a data in the database, not a timestamp)
 */
public class EmailExpireDateAttribute extends AbstractProfileAttribute {

    public static String NAME = "profile.email.expireDate";

    public EmailExpireDateAttribute(String providerJndi) {

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
