package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class MobileAttribute extends AbstractProfileAttribute {

    public MobileAttribute(String providerJndi) {

        super( providerJndi, "device.otpoversms.mobile", DataType.STRING );
    }

    public String getName() {

        return "profile.mobile";
    }
}
