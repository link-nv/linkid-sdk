package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;


public class LinkIDDevice implements Serializable {

    private final String  name;
    private final String  friendlyName;
    private final String  id;
    private final String  info;
    private       boolean disabled;

    public LinkIDDevice(final String name, final String friendlyName, final String id, final String info, final boolean disabled) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.id = id;
        this.info = info;
        this.disabled = disabled;
    }

    public String getName() {

        return name;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public String getId() {

        return id;
    }

    public String getInfo() {

        return info;
    }

    public boolean isDisabled() {

        return disabled;
    }

    public void setDisabled(final boolean disabled) {

        this.disabled = disabled;
    }
}
