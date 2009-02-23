/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore.service.bean;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.link.safeonline.keystore.OlasKeyStore;
import net.link.safeonline.keystore.entity.KeyConfig;
import net.link.safeonline.keystore.entity.Type;
import net.link.safeonline.keystore.service.KeyService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG = LogFactory.getLog(KeyServiceBean.class);

    @PersistenceContext(unitName = "KeyStoreEntityManager")
    protected EntityManager  em;


    @SuppressWarnings("unchecked")
    public List<String> getAccessors() {

        return em.createNamedQuery(KeyConfig.getAccessors).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public PrivateKeyEntry getPrivateKeyEntry(Class<? extends OlasKeyStore> keyStoreAccessor) {

        return getPrivateKeyEntry(keyStoreAccessor.getCanonicalName());
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public PrivateKeyEntry getPrivateKeyEntry(String keyStoreAccessor) {

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor);
        if (keyStoreConfig == null)
            throw new IllegalStateException("No configuration for key store: " + keyStoreAccessor);

        return keyStoreConfig.getType().getPrivateKeyEntry(keyStoreConfig.getConfig());
    }

    /**
     * {@inheritDoc}
     */
    public Type getType(Class<? extends OlasKeyStore> keyStoreAccessor) {

        return getType(keyStoreAccessor.getCanonicalName());
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public Type getType(String keyStoreAccessor) {

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor);
        if (keyStoreConfig == null)
            return null;

        return keyStoreConfig.getType();
    }

    /**
     * {@inheritDoc}
     */
    public String getConfig(Class<? extends OlasKeyStore> keyStoreAccessor) {

        return getConfig(keyStoreAccessor.getCanonicalName());
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public String getConfig(String keyStoreAccessor) {

        LOG.debug("getConfig " + keyStoreAccessor);

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor);

        if (keyStoreConfig == null)
            return null;

        return keyStoreConfig.getConfig();
    }

    /**
     * {@inheritDoc}
     */
    public void configure(Class<? extends OlasKeyStore> keyStoreAccessor, Type type, String config) {

        configure(keyStoreAccessor.getCanonicalName(), type, config);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public void configure(String keyStoreAccessor, Type type, String config) {

        LOG.debug("configure " + keyStoreAccessor + " config=" + config);

        KeyConfig keyStoreConfig = em.find(KeyConfig.class, keyStoreAccessor);

        if (keyStoreConfig == null) {
            keyStoreConfig = new KeyConfig(keyStoreAccessor, type, config);
            em.persist(keyStoreConfig);
        }

        else {
            keyStoreConfig.setType(type);
            keyStoreConfig.setConfig(config);
        }
    }
}
