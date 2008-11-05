/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline;

/**
 * Components implementing this interface can be scheduled by the task scheduler
 * 
 * @author dhouthoo
 * 
 */
public interface Task {

    public static final String JNDI_PREFIX = "SafeOnline/task/";


    /**
     * Return a name by which the task can be identified
     * 
     */
    String getName();

    /**
     * Perform the task
     */
    void perform()
            throws Exception;
}
