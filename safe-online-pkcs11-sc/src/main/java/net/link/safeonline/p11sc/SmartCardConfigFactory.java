/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.p11sc;

import java.util.List;


public interface SmartCardConfigFactory {

    /**
     * Gives back a list of smart card configurations.
     * 
     */
    List<SmartCardConfig> getSmartCardConfigs();
}
