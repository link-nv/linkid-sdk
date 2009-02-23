/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.keystore.startup;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import net.link.safeonline.Startable;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.entity.Type;
import net.link.safeonline.keystore.service.KeyService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link SafeOnlineNodeKeyStoreStartableBean}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
@Stateless
@Local(Startable.class)
@LocalBinding(jndiBinding = SafeOnlineNodeKeyStoreStartableBean.JNDI_BINDING)
public class SafeOnlineNodeKeyStoreStartableBean implements Startable {

    public static final String  JNDI_BINDING      = Startable.JNDI_PREFIX + "SafeOnlineNodeKeyStoreStartableBean";

    private static final String NODE_KEY_PASSWORD = "secret";
    private static final String NODE_KEY_RESOURCE = "safe-online-node-keystore.jks";

    @EJB(mappedName = KeyService.JNDI_BINDING)
    private KeyService          keyService;


    /**
     * {@inheritDoc}
     */
    public void postStart() {

        configureKeys();
    }

    private void configureKeys() {

        if (keyService.getConfig(SafeOnlineNodeKeyStore.class) == null) {
            keyService.configure(SafeOnlineNodeKeyStore.class, Type.JKS, String.format("%s:%s:%s", NODE_KEY_PASSWORD, NODE_KEY_PASSWORD,
                    NODE_KEY_RESOURCE));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void preStop() {

    }

    /**
     * {@inheritDoc}
     */
    public int getPriority() {

        return Startable.PRIORITY_BOOTSTRAP + 1;
    }
}
