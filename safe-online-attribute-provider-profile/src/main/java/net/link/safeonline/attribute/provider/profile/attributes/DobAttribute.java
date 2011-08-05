package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class DobAttribute extends AbstractProfileAttribute {

    public DobAttribute(String providerJndi) {

        super( providerJndi, "device.beid.dateOfBirth", DataType.DATE );
    }

    public String getName() {

        return "profile.dob";
    }
}
