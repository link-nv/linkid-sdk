/*
 * safe-online - linkid-sdk-api
 *
 * Copyright 2006-2016 linkID Inc. All rights reserved.
 * linkID Inc. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.notification;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 01/08/16
 * Time: 11:35
 */
public class LinkIDNotificationTopicConfiguration implements Serializable {

    private final LinkIDNotificationTopic topic;
    @Nullable
    private final String                  filter;

    public LinkIDNotificationTopicConfiguration(final LinkIDNotificationTopic topic, @Nullable final String filter) {

        this.topic = topic;
        this.filter = filter;
    }

    // Helper methods

    @Override
    public String toString() {

        return MoreObjects.toStringHelper( this ).add( "topic", topic ).add( "filter", filter ).toString();
    }

    // Accessors

    public LinkIDNotificationTopic getTopic() {

        return topic;
    }

    @Nullable
    public String getFilter() {

        return filter;
    }
}
