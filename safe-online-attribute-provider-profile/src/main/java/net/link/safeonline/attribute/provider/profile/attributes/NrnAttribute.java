package net.link.safeonline.attribute.provider.profile.attributes;

import net.link.safeonline.attribute.provider.DataType;


public class NrnAttribute extends AbstractProfileAttribute {

    public NrnAttribute(String providerJndi) {

        super( providerJndi, "device.beid.nrn", DataType.STRING );
    }

    public String getName() {

        return "profile.nrn";
    }
}
