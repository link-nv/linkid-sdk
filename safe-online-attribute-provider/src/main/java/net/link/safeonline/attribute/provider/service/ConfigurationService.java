/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attribute.provider.service;

import net.link.safeonline.attribute.provider.AttributeProvider;


/**
 * LinkID Configuration Service. <p/>
 *
 * This service should be used if {@link AttributeProvider}'s wish to add and retrieve linkID configuration information.
 */
public interface ConfigurationService {

    void initConfigurationValue(String group, String name, Object value);

    Object getConfigurationValue(String group, String name, Object defaultValue);
}
