/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.ws.json;

import java.io.Serializable;
import java.util.Locale;


public interface WSConfiguration extends Serializable {

    PublicApplicationConfiguration getApplicationConfig(String name, Locale locale)
            throws ConfigurationOperationFailedException;
}
