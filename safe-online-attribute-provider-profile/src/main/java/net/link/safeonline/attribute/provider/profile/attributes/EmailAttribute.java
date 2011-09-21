package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.AttributeType;
import net.link.safeonline.attribute.provider.DataType;


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
}
