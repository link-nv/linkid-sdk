package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class EmailAttribute extends AbstractProfileAttribute {

    public EmailAttribute(String providerJndi) {

        super( providerJndi, null, DataType.STRING );
    }

    public String getName() {

        return "profile.email";
    }
}
