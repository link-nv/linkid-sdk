/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore.service;

import java.security.KeyStore.PrivateKeyEntry;

import javax.ejb.Local;

import net.link.safeonline.keystore.AbstractKeyStore;
import net.link.safeonline.keystore.entity.Type;


/**
 * <h2>{@link KeyService}<br>
 * <sub>This service is used to resolve {@link PrivateKeyEntry}s from DS-stored type-specific configuration.</sub></h2>
 * 
 * <p>
 * There are several different types of sources for key data. These different types have implementation-specific methods of retrieving
 * {@link PrivateKeyEntry}s. See the documentation on {@link Type} for more about that.
 * </p>
 * 
 * <p>
 * This service bean provides access to these keys using the type-specific configuration as it is stored in the DS.
 * </p>
 * 
 * <p>
 * <i>Jan 30, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
@Local
public interface KeyService {

    public static final String JNDI_BINDING = "SafeOnline/KeyServiceBean/local";


    public PrivateKeyEntry getPrivateKeyEntry(Class<? extends AbstractKeyStore> keyStoreAccessor);

    public Type getType(Class<? extends AbstractKeyStore> keyStoreAccessor);

    public String getConfig(Class<? extends AbstractKeyStore> keyStoreAccessor);

    public void configure(Class<? extends AbstractKeyStore> keyStoreAccessor, Type type, String config);
}
