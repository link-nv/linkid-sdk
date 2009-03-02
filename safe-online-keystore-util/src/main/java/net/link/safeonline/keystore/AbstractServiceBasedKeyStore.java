/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import java.security.KeyStore.PrivateKeyEntry;

import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.util.ee.EjbUtils;


/**
 * <h2>{@link AbstractServiceBasedKeyStore}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 15, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractServiceBasedKeyStore extends AbstractKeyStore {

    public PrivateKeyEntry _getPrivateKeyEntry() {

        return EjbUtils.getEJB(KeyService.JNDI_BINDING, KeyService.class).getPrivateKeyEntry(getClass());
    }
}
