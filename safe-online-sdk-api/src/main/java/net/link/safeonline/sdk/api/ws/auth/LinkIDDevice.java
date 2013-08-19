package net.link.safeonline.sdk.api.ws.auth;

import java.io.Serializable;
import java.util.Date;


public class LinkIDDevice implements Serializable {

    private final String  name;
    private final String  friendlyName;
    private final String  id;
    private final String  info;
    private       boolean disabled;

    // usage date
    private final Date activationDate;
    private final long authentications;
    private final Date lastAuthenticated;

    public LinkIDDevice(final String name, final String friendlyName, final String id, final String info, final boolean disabled, final Date activationDate,
                        final long authentications, final Date lastAuthenticated) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.id = id;
        this.info = info;
        this.disabled = disabled;
        this.activationDate = activationDate;
        this.authentications = authentications;
        this.lastAuthenticated = lastAuthenticated;
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

    public Date getActivationDate() {

        return activationDate;
    }

    public long getAuthentications() {

        return authentications;
    }

    public Date getLastAuthenticated() {

        return lastAuthenticated;
    }
}
