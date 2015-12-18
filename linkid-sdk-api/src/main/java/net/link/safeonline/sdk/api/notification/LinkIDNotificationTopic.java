/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.notification;

import net.link.util.InternalInconsistencyException;


public enum LinkIDNotificationTopic {

    REMOVE_USER( "urn:net:lin-k:linkid:topic:user:remove" ),
    UNSUBSCRIBE_USER( "urn:net:lin-k:linkid:topic:user:unsubscribe" ),
    ATTRIBUTE_UPDATE( "urn:net:lin-k:linkid:topic:user:attribute:update" ),
    ATTRIBUTE_REMOVAL( "urn:net:lin-k:linkid:topic:user:attribute:remove" ),
    IDENTITY_UPDATE( "urn:net:lin-k:linkid:topic:user:identity:update" ),
    EXPIRED_AUTHENTICATION( "urn:net:lin-k:linkid:topic:expired:authentication" ),
    EXPIRED_PAYMENT( "urn:net:lin-k:linkid:topic:expired:payment" ),
    MANDATE_ARCHIVED( "urn:net:lin-k:linkid:topic:mandate:archived" ),
    LTQR_SESSION_NEW( "urn:net:lin-k:linkid:topic:ltqr:session:new" ),
    LTQR_SESSION_CANCEL( "urn:net:lin-k:linkid:topic:ltqr:session:cancel" ),
    LTQR_SESSION_UPDATE( "urn:net:lin-k:linkid:topic:ltqr:session:update" ),
    CONFIGURATION_UPDATE( "urn:net:lin-k:linkid:topic:config:update" ),
    PAYMENT_ORDER_UPDATE( "urn:net:lin-k:linkid:topic:payment:update" );

    private final String topicUri;

    LinkIDNotificationTopic(final String topicUri) {

        this.topicUri = topicUri;
    }

    public String getTopicUri() {

        return topicUri;
    }

    public static LinkIDNotificationTopic to(final String topicString) {

        if (null == topicString) {
            throw new InternalInconsistencyException( "No topic specified, aborting..." );
        }

        for (LinkIDNotificationTopic topic : values()) {
            if (topic.getTopicUri().equals( topicString ))
                return topic;
        }

        throw new InternalInconsistencyException( String.format( "Invalid topic URI: %s", topicString ) );
    }
}
