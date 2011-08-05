package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class PostalCodeAttribute extends AbstractProfileAttribute {

    public PostalCodeAttribute(String providerJndi) {

        super( providerJndi, "device.beid.zip", DataType.STRING );
    }

    public String getName() {

        return "profile.postalCode";
    }
}
