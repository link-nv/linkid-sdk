/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model.bean;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import net.link.safeonline.pkix.model.PkiProvider;
import net.link.safeonline.pkix.model.PkiProviderManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Stateless
public class PkiProviderManagerBean implements PkiProviderManager {

    private static final Log LOG = LogFactory.getLog(PkiProviderManagerBean.class);


    public PkiProvider findPkiProvider(X509Certificate certificate) {

        List<PkiProvider> pkiProviders = getPkiProviders();
        for (PkiProvider pkiProvider : pkiProviders) {
            if (pkiProvider.accept(certificate))
                return pkiProvider.getReference();
        }
        return null;
    }

    private List<PkiProvider> getPkiProviders() {

        List<PkiProvider> pkiProviders = new LinkedList<PkiProvider>();
        try {
            InitialContext initialContext = new InitialContext();
            Context context = (Context) initialContext.lookup(PkiProvider.PKI_PROVIDER_JNDI);
            NamingEnumeration<NameClassPair> result = initialContext.list(PkiProvider.PKI_PROVIDER_JNDI);
            while (result.hasMore()) {
                NameClassPair nameClassPair = result.next();
                String objectName = nameClassPair.getName();
                LOG.debug(objectName + ":" + nameClassPair.getClassName());
                Object object = context.lookup(objectName);
                if (object instanceof PkiProvider) {
                    PkiProvider pkiProvider = (PkiProvider) object;
                    pkiProviders.add(pkiProvider);
                }
            }
        } catch (NamingException e) {
            LOG.error("JNDI error: " + e.getMessage());
        }
        return pkiProviders;
    }
}
