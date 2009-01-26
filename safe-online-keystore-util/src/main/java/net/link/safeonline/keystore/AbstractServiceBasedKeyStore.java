/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore;

import java.security.KeyStore.PrivateKeyEntry;


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

    public AbstractServiceBasedKeyStore() {

    }

    public PrivateKeyEntry _getPrivateKeyEntry() {

        return null;
    }
}
