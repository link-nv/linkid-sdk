package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class FamilyNameAttribute extends AbstractProfileAttribute {

    public FamilyNameAttribute(String providerJndi) {

        super( providerJndi, "device.beid.surname", DataType.STRING );
    }

    public String getName() {

        return "profile.familyName";
    }
}
