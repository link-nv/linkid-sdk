/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline;

/**
 * Components implementing this interface can be notified during application startup and shutdown.
 *
 * @author fcorneli
 *
 */
public interface Startable {

    public static final String JNDI_PREFIX        = "SafeOnline/startup/";

    public static final int    PRIORITY_DONT_CARE = 1000;

    public static final int    PRIORITY_BOOTSTRAP = 10000;


    /**
     * Gives back the boot priority of this startable component. Higher priority startable component will start before
     * lower priority components. Reverse for stopping the components.
     *
     * @return the priority.
     */
    int getPriority();

    /**
     * Callback to notify that the SafeOnline system has started.
     */
    void postStart();

    /**
     * Callback to notify that the SafeOnline system is about to be stopped.
     */
    void preStop();
}
