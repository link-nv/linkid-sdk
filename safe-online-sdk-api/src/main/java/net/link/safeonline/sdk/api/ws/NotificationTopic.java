package net.link.safeonline.sdk.api.ws;

import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;


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

        throw new InternalInconsistencyException( String.format( "Invalid topic URI: %s", topicString ) );
    }
}
