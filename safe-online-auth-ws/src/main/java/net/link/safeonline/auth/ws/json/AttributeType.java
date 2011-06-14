package net.link.safeonline.auth.ws.json;

/**
 * <i>06 10, 2011</i>
 *
 * @author lhunath
 */
public class AttributeType {

    public final String name;
    public final String friendlyName;
    public final boolean required;
    public final boolean dataMining;

    public AttributeType(final String name, final String friendlyName, final boolean required, final boolean dataMining) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.required = required;
        this.dataMining = dataMining;
    }

    public String getName() {

        return name;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public boolean isRequired() {

        return required;
    }

    public boolean isDataMining() {

        return dataMining;
    }
}
