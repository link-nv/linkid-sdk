/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.lawyer;

public interface AbstractLawyerDataClient {

    /*
     * Lifecycle.
     */
    void destroyCallback();

    void postConstructCallback();

    void postActivateCallback();

    void prePassivateCallback();
}
