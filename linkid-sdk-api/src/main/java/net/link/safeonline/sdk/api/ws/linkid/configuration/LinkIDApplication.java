package net.link.safeonline.sdk.api.ws.linkid.configuration;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 21/01/16
 * Time: 15:18
 */
public class LinkIDApplication implements Serializable {

    private final String name;
    private final String friendlyName;

    public LinkIDApplication(final String name, final String friendlyName) {

        this.name = name;
        this.friendlyName = friendlyName;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDApplication{" +
               "name='" + name + '\'' +
               ", friendlyName='" + friendlyName + '\'' +
               '}';
    }

    // Accessors

    public String getName() {

        return name;
    }

    public String getFriendlyName() {

        return friendlyName;
    }
}
