/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.helpdesk;

public interface HelpdeskBase {

    /*
     * Getters for helpdesk information
     */
    Long getId();

    String getPhone();

    String getEmail();

    String getDummy();

    /*
     * Lifecycle callbacks
     */
    void init();

    void destroyCallback();

    /*
     * Actions
     */
    String createTicket();
}
