package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class PhoneAttribute extends AbstractProfileAttribute {

    public PhoneAttribute(String providerJndi) {

        super( providerJndi, null, DataType.STRING );
    }

    public String getName() {

        return "profile.phone";
    }
}
