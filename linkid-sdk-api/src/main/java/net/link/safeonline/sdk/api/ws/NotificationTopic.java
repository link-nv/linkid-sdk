/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.ws;

public enum NotificationTopic {

    REMOVE_USER( "urn:net:lin-k:safe-online:topic:user:remove" ),
    UNSUBSCRIBE_USER( "urn:net:lin-k:safe-online:topic:user:unsubscribe" );

    private final String topicUri;

    private NotificationTopic(final String topicUri) {

        this.topicUri = topicUri;
    }

    public String getTopicUri() {

        return topicUri;
    }

    public static NotificationTopic to(final String topicString) {

        for (NotificationTopic topic : values()) {
            if (topic.getTopicUri().equals( topicString ))
                return topic;
        }

        throw new RuntimeException( String.format( "Invalid topic URI: %s", topicString ) );
    }
}
