/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import be.fedict.trust.TrustValidator;
import be.fedict.trust.linker.TrustLinkerResultException;
import be.fedict.trust.repository.MemoryCertificateRepository;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import net.link.safeonline.sdk.api.configuration.LinkIDConfigService;
import net.link.util.common.CertificateChain;
import net.link.util.common.LazyPublicKeyTrustLinker;
import net.link.util.keyprovider.KeyProvider;
import net.link.util.logging.Logger;
import net.link.util.util.ObjectUtils;
import net.link.util.ws.security.x509.AbstractWSSecurityConfiguration;
import org.joda.time.Duration;


/**
 * <h2>{@link LinkIDSDKWSSecurityConfiguration}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>03 31, 2011</i> </p>
 *
 * @author lhunath
 */
public class LinkIDSDKWSSecurityConfiguration extends AbstractWSSecurityConfiguration {

    static final Logger logger = Logger.get( LinkIDSDKWSSecurityConfiguration.class );

    private final LinkIDConfigService config;
    private final KeyProvider         keyProvider;

    public LinkIDSDKWSSecurityConfiguration(final LinkIDConfigService config, final KeyProvider keyProvider) {

        this.config = config;
        this.keyProvider = keyProvider;
    }

    @Override
    public boolean isCertificateChainTrusted(final CertificateChain aCertificateChain) {

        // Manually check whether the end certificate has the correct DN.
        if (!ObjectUtils.isEqual( aCertificateChain.getIdentityCertificate().getSubjectX500Principal(), config.trustedDN() )) {
            logger.err( "End certificate's DN does not match the one specified in the configuration: \"%s\" - \"%s\"",
                    aCertificateChain.getIdentityCertificate().getSubjectX500Principal(), config.trustedDN() );
            return false;
        }

        MemoryCertificateRepository certificateRepository = new MemoryCertificateRepository();
        Collection<X509Certificate> trustedCertificates = keyProvider.getTrustedCertificates();
        for (X509Certificate trustedCertificate : trustedCertificates)
            certificateRepository.addTrustPoint( trustedCertificate );

        try {
            TrustValidator trustValidator = new TrustValidator( certificateRepository );
            trustValidator.addTrustLinker( new LazyPublicKeyTrustLinker() );
            trustValidator.isTrusted( aCertificateChain.getOrderedCertificateChain() );
            return true;
        }
        catch (TrustLinkerResultException e) {
            logger.dbg( e, "Couldn't trust certificate chain.\nChain:\n%s\nTrusted Certificates:\n%s", aCertificateChain, trustedCertificates );
            return false;
        }
    }

    @Override
    public CertificateChain getIdentityCertificateChain() {

        return keyProvider.getIdentityCertificateChain();
    }

    @Override
    public PrivateKey getPrivateKey() {

        return keyProvider.getIdentityKeyPair().getPrivate();
    }

    @Override
    public Duration getMaximumAge() {

        return config.maxTimeOffset();
    }
}
