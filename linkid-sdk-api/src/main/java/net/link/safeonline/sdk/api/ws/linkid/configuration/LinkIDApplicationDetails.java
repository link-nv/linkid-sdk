/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.linkid.configuration;

import java.io.Serializable;


/**
 * Created by wvdhaute
 * Date: 21/01/16
 * Time: 15:18
 */
public class LinkIDApplicationDetails implements Serializable {

    private final String name;
    private final String friendlyName;
    private final String description;
    private final String applicationURL;
    private final String logo;

    public LinkIDApplicationDetails(final String name, final String friendlyName, final String description, final String applicationURL, final String logo) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.description = description;
        this.applicationURL = applicationURL;
        this.logo = logo;
    }

    // Helper methods

    @Override
    public String toString() {

        return "LinkIDApplicationDetails{" +
               "name='" + name + '\'' +
               ", friendlyName='" + friendlyName + '\'' +
               ", description='" + description + '\'' +
               ", applicationURL='" + applicationURL + '\'' +
               ", logo='" + logo + '\'' +
               '}';
    }

    // Accessors

    public String getName() {

        return name;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public String getDescription() {

        return description;
    }

    public String getApplicationURL() {

        return applicationURL;
    }

    public String getLogo() {

        return logo;
    }
}
