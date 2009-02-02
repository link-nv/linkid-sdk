/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore.service.bean;

import java.security.KeyStore.PrivateKeyEntry;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.keystore.AbstractKeyStore;
import net.link.safeonline.keystore.entity.KeyConfig;
import net.link.safeonline.keystore.entity.Type;
import net.link.safeonline.keystore.service.KeyService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link KeyServiceBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 30, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
@Stateless
@LocalBinding(jndiBinding = KeyService.JNDI_BINDING)
public class KeyServiceBean implements KeyService {

    @PersistenceContext(unitName = "KeyStoreEntityManager")
    protected EntityManager em;


    /**
     * {@inheritDoc}
     */
    public PrivateKeyEntry getPrivateKeyEntry(Class<? extends AbstractKeyStore> keyStoreAccessor) {

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor.getCanonicalName());
        if (keyStoreConfig == null)
            throw new IllegalStateException("No configuration for key store: " + keyStoreAccessor);

        return keyStoreConfig.getType().getPrivateKeyEntry(keyStoreConfig.getConfig());
    }

    /**
     * {@inheritDoc}
     */
    public Type getType(Class<? extends AbstractKeyStore> keyStoreAccessor) {

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor.getCanonicalName());
        if (keyStoreConfig == null)
            return null;

        return keyStoreConfig.getType();
    }

    /**
     * {@inheritDoc}
     */
    public String getConfig(Class<? extends AbstractKeyStore> keyStoreAccessor) {

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor.getCanonicalName());
        if (keyStoreConfig == null)
            return null;

        return keyStoreConfig.getConfig();
    }

    /**
     * {@inheritDoc}
     */
    public void configure(Class<? extends AbstractKeyStore> keyStoreAccessor, Type type, String config) {

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor.getCanonicalName());

        if (keyStoreConfig == null) {
            keyStoreConfig = new KeyConfig(keyStoreAccessor.getCanonicalName(), type, config);
            em.persist(keyStoreConfig);
        }

        else {
            keyStoreConfig.setType(type);
            keyStoreConfig.setConfig(config);
        }
    }
}
