/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.performance.bean;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import net.link.safeonline.model.performance.PerformanceServiceRemote;
import net.link.safeonline.performance.keystore.PerformanceKeyStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateless
@RemoteBinding(jndiBinding = PerformanceServiceRemote.JNDI_BINDING)
public class PerformanceServiceBean implements PerformanceServiceRemote {

    private static final Log LOG = LogFactory.getLog(PerformanceServiceBean.class);

    private PrivateKey       privateKey;

    private X509Certificate  certificate;


    public X509Certificate getCertificate() {

        LOG.debug("get certificate");
        return certificate;
    }

    public PrivateKey getPrivateKey() {

        LOG.debug("get private key");
        return privateKey;
    }

    @PostConstruct
    public void postConstructCallback() {

        LOG.debug("post construct callback");
        PrivateKeyEntry perfPrivateKeyEntry = PerformanceKeyStore.getPrivateKeyEntry();
        privateKey = perfPrivateKeyEntry.getPrivateKey();
        certificate = (X509Certificate) perfPrivateKeyEntry.getCertificate();
    }
}
