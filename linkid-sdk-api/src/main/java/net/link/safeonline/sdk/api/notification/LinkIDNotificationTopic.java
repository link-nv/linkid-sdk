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
    PAYMENT_ORDER_UPDATE( "urn:net:lin-k:linkid:topic:payment:update" ),
    AUTHENTICATION_RETRIEVED( "urn:net:lin-k:linkid:topic:authentication:retrieved" ),
    AUTHENTICATION_SUCCESS( "urn:net:lin-k:linkid:topic:authentication:success" ),
    AUTHENTICATION_PAYMENT_FINISHED( "urn:net:lin-k:linkid:topic:authentication:payment:finished" ),
    AUTHENTICATION_CANCELED( "urn:net:lin-k:linkid:topic:authentication:canceled" ),
    AUTHENTICATION_FAILED( "urn:net:lin-k:linkid:topic:authentication:failed" ),
    THEME_UPDATE( "urn:net:lin-k:linkid:topic:theme:update" ),
    THEME_REMOVAL_SUCCESS( "urn:net:lin-k:linkid:topic:theme:removal:success" ),
    THEME_REMOVAL_REJECTED( "urn:net:lin-k:linkid:topic:theme:removal:rejected" ),
    THEME_REJECTED( "urn:net:lin-k:linkid:topic:theme:rejected" ),
    WALLET_ORGANIZATION_REMOVAL_SUCCESS( "urn:net:lin-k:linkid:topic:wallet:organization:removal:success" ),
    WALLET_ORGANIZATION_REMOVAL_REJECTED( "urn:net:lin-k:linkid:topic:wallet:organization:removal:rejected" ),
    WALLET_ORGANIZATION_REJECTED( "urn:net:lin-k:linkid:topic:wallet:organization:rejected" ),
    VOUCHER_REWARD( "urn:net:lin-k:linkid:topic:voucher:reward" ),
    VOUCHER_ACTIVATE( "urn:net:lin-k:linkid:topic:voucher:activate" ),
    VOUCHER_REDEEM( "urn:net:lin-k:linkid:topic:voucher:redeem" ),
    PERMISSION_UPDATE( "urn:net:lin-k:linkid:topic:application:permission:update" );

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
