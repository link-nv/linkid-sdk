package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class CityAttribute extends AbstractProfileAttribute {

    public CityAttribute(String providerJndi) {

        super( providerJndi, "device.beid.municipality", DataType.STRING );
    }

    public String getName() {

        return "profile.city";
    }
}
