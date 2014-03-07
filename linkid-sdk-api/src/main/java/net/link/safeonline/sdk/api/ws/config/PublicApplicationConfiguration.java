/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws.config;

import java.io.Serializable;
import java.net.URL;
import java.util.List;


@SuppressWarnings("UnusedDeclaration")
public class PublicApplicationConfiguration implements Serializable {

    private final String                          name;
    private final String                          friendlyName;
    private final String                          description;
    private final URL                             url;
    private final String                          ua;
    private final List<ApplicationAttributeGroup> groups;

    public PublicApplicationConfiguration(final String name, final String friendlyName, final String description, final URL url,
                                          final String ua, final List<ApplicationAttributeGroup> groups) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.description = description;
        this.url = url;
        this.ua = ua;
        this.groups = groups;
    }

    public String getName() {

        return name;
    }

    public String getFriendlyName() {

        return friendlyName;
    }

    public String getDescription() {

        return description;
    }

    public URL getUrl() {

        return url;
    }

    public String getUa() {

        return ua;
    }

    public List<ApplicationAttributeGroup> getGroups() {

        return groups;
    }

    public void reject(final String attributeName) {

        for (ApplicationAttributeGroup group : groups) {
            for (PublicApplicationAttribute attribute : group.getAttributes()) {
                if (attribute.getName().equals( attributeName )) {
                    attribute.setRejected( true );
                    return;
                }
            }
        }

        throw new RuntimeException( String.format( "Unable to reject unknown attribute :%s", attributeName ) );
    }
}
