/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.service;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.List;

import net.link.safeonline.keystore.entity.Type;


public interface KeyConfigMBean {

    public List<String> getAccessors();

    public PrivateKeyEntry getPrivateKeyEntry(String keyStoreAccessor);

    public Type getType(String keyStoreAccessor);

    public String getConfig(String keyStoreAccessor);

    public void configure(String keyStoreAccessor, Type type, String config);
}
