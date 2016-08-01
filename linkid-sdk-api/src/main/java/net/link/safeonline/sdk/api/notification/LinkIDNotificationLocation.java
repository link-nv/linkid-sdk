/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.notification;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;


/**
 * Created by wvdhaute
 * Date: 01/08/16
 * Time: 16:18
 */
public class LinkIDNotificationLocation implements Serializable {

    private final String                                     urn;
    private final String                                     label;
    private final String                                     location;
    private final List<LinkIDNotificationTopicConfiguration> topics;

    public LinkIDNotificationLocation(final String urn, final String label, final String location, final List<LinkIDNotificationTopicConfiguration> topics) {

        this.urn = urn;
        this.label = label;
        this.location = location;
        this.topics = topics;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this ).add( "urn", urn ).add( "label", label ).add( "location", location ).add( "topics", topics ).toString();
    }

    // Accessors

    public String getUrn() {

        return urn;
    }

    public String getLabel() {

        return label;
    }

    public String getLocation() {

        return location;
    }

    public List<LinkIDNotificationTopicConfiguration> getTopics() {

        return topics;
    }
}
