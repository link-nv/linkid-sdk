/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

public enum HistoryEventType {
    LOGIN_SUCCESS("login_success"), DEVICE_REGISTRATION("device_registration"), DEVICE_REMOVAL("device_removal"), DEVICE_UPDATE(
            "device_update"), SUBSCRIPTION_ADD("subscription_add"), SUBSCRIPTION_REMOVE("subscription_remove"), ATTRIBUTE_CHANGE(
            "attribute_change"), ATTRIBUTE_REMOVE("attribute_remove"), ATTRIBUTE_ADD("attribute_add"), ATTRIBUTE_PROVIDER_CHANGE(
            "attribute_provider_change"), ATTRIBUTE_PROVIDER_REMOVE("attribute_provider_remove"), ATTRIBUTE_PROVIDER_ADD(
            "attribute_provider_add"), IDENTITY_CONFIRMATION("identity_confirmation"), HELPDESK_ID("helpdesk_id");

    private final String key;


    HistoryEventType(String key) {

        this.key = key;
    }

    public String getKey() {

        return this.key;
    }
}
