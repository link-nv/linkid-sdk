package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class StreetAndNumberAttribute extends AbstractProfileAttribute {

    public StreetAndNumberAttribute(String providerJndi) {

        super( providerJndi, "device.beid.streetAndNumber", DataType.STRING );
    }

    public String getName() {

        return "profile.streetAndNumber";
    }
}
