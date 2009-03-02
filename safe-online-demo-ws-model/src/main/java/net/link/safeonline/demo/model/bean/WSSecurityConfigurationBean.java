/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.model.bean;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.ejb.Stateless;

import net.link.safeonline.demo.model.WSSecurityConfiguration;
import net.link.safeonline.keystore.SafeOnlineKeyStore;

import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = WSSecurityConfiguration.JNDI_BINDING)
public class WSSecurityConfigurationBean implements WSSecurityConfiguration {

    /**
     * {@inheritDoc}
     */
    public long getMaximumWsSecurityTimestampOffset() {

        return 1000 * 60 * 5L;
    }

    /**
     * {@inheritDoc}
     */
    public boolean skipMessageIntegrityCheck(X509Certificate certificate) {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public X509Certificate getCertificate() {

        SafeOnlineKeyStore olasKeyStore = new SafeOnlineKeyStore();
        return olasKeyStore.getCertificate();
    }

    /**
     * {@inheritDoc}
     */
    public PrivateKey getPrivateKey() {

        SafeOnlineKeyStore olasKeyStore = new SafeOnlineKeyStore();
        return olasKeyStore.getPrivateKey();
    }

}
