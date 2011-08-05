package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class GivenNameAttribute extends AbstractProfileAttribute {

    public GivenNameAttribute(String providerJndi) {

        super( providerJndi, "device.beid.givenName", DataType.STRING );
    }

    public String getName() {

        return "profile.givenName";
    }
}
