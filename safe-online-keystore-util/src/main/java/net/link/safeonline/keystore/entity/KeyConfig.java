/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;


/**
 * <h2>{@link KeyConfig}<br>
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
@Entity
public class KeyConfig {

    @Id
    private String keyStoreAccessor;

    @Enumerated(EnumType.STRING)
    private Type   type;

    private String config;


    public KeyConfig() {

    }

    public KeyConfig(String keyStoreAccessor, Type type, String config) {

        this.keyStoreAccessor = keyStoreAccessor;
        this.type = type;
        this.config = config;
    }

    /**
     * @param type
     *            The {@link Type} of this {@link KeyConfig}.
     */
    public void setType(Type type) {

        this.type = type;
    }

    /**
     * @return The {@link Type} of this {@link KeyConfig}.
     */
    public Type getType() {

        return type;
    }

    /**
     * @param config
     *            The {@link Type}-specific config string of this {@link KeyConfig}.
     */
    public void setConfig(String config) {

        this.config = config;
    }

    /**
     * @return The {@link Type}-specific config string of this {@link KeyConfig}.
     */
    public String getConfig() {

        return config;
    }

    /**
     * @param keyStoreAccessor
     *            The keyStoreAccessor of this {@link KeyConfig}.
     */
    public void setKeyStoreAccessor(String keyStoreAccessor) {

        this.keyStoreAccessor = keyStoreAccessor;
    }

    /**
     * @return The keyStoreAccessor of this {@link KeyConfig}.
     */
    public String getKeyStoreAccessor() {

        return keyStoreAccessor;
    }
}
