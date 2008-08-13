/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

public enum SubscriptionOwnerType {
    /**
     * Marks that a subscription is owned by the application itself. This allows the business logic to only
     * change/remove a subscription if the operation is performed in the name of the application instead of the subject.
     */
    APPLICATION,
    /**
     * Marks that a subscription is owned by its subject.
     */
    SUBJECT
}
