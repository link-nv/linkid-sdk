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
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.util.ee.EjbUtils;


/**
 * Service that prepares the runtime for the SafeOnline application.
 * 
 * @author fcorneli
 * 
 */
public class KeyConfig implements KeyConfigMBean {

    /**
     * {@inheritDoc}
     */
    public List<String> getAccessors() {

        return EjbUtils.getEJB(KeyService.JNDI_BINDING, KeyService.class).getAccessors();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public Type getType(String keyStoreAccessor) {

        return EjbUtils.getEJB(KeyService.JNDI_BINDING, KeyService.class).getType(keyStoreAccessor);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public String getConfig(String keyStoreAccessor) {

        return EjbUtils.getEJB(KeyService.JNDI_BINDING, KeyService.class).getConfig(keyStoreAccessor);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public PrivateKeyEntry getPrivateKeyEntry(String keyStoreAccessor) {

        return EjbUtils.getEJB(KeyService.JNDI_BINDING, KeyService.class).getPrivateKeyEntry(keyStoreAccessor);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    public void configure(String keyStoreAccessor, Type type, String config) {

        EjbUtils.getEJB(KeyService.JNDI_BINDING, KeyService.class).configure(keyStoreAccessor, type, config);
    }
}
