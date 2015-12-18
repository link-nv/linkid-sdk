/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import static net.link.util.util.ObjectUtils.ifNotNullElse;

import be.fedict.trust.TrustValidator;
import be.fedict.trust.linker.TrustLinkerResultException;
import be.fedict.trust.repository.MemoryCertificateRepository;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import javax.security.auth.x500.X500Principal;
import net.link.safeonline.sdk.configuration.LinkIDSDKConfigHolder;
import net.link.util.common.CertificateChain;
import net.link.util.common.LazyPublicKeyTrustLinker;
import net.link.util.config.KeyProvider;
import net.link.util.logging.Logger;
import net.link.util.util.NNSupplier;
import net.link.util.util.ObjectUtils;
import net.link.util.ws.security.x509.AbstractWSSecurityConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private final X500Principal trustedDN;
    private final KeyProvider   keyProvider;

    public LinkIDSDKWSSecurityConfiguration() {

        this( null, null );
    }

    public LinkIDSDKWSSecurityConfiguration(@Nullable final X500Principal trustedDN, @Nullable final KeyProvider keyProvider) {

        this.trustedDN = trustedDN;
        this.keyProvider = keyProvider;
    }

    @Override
    public boolean isCertificateChainTrusted(final CertificateChain aCertificateChain) {

        // Manually check whether the end certificate has the correct DN.
        if (!ObjectUtils.isEqual( aCertificateChain.getIdentityCertificate().getSubjectX500Principal(), getTrustedDN() )) {
            logger.err( "End certificate's DN does not match the one specified in the configuration: \"%s\" - \"%s\"",
                    aCertificateChain.getIdentityCertificate().getSubjectX500Principal(), getTrustedDN() );
            return false;
        }

        MemoryCertificateRepository certificateRepository = new MemoryCertificateRepository();
        Collection<X509Certificate> trustedCertificates = getKeyProvider().getTrustedCertificates();
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

    public X500Principal getTrustedDN() {

        return ifNotNullElse( trustedDN, LinkIDSDKConfigHolder.config().linkID().app().trustedDN() );
    }

    public KeyProvider getKeyProvider() {

        return ifNotNullElse( keyProvider, new NNSupplier<KeyProvider>() {
            @Override
            @NotNull
            public KeyProvider get() {

                return LinkIDSDKConfigHolder.config().linkID().app().keyProvider();
            }
        } );
    }

    @Override
    public CertificateChain getIdentityCertificateChain() {

        return getKeyProvider().getIdentityCertificateChain();
    }

    @Override
    public PrivateKey getPrivateKey() {

        return getKeyProvider().getIdentityKeyPair().getPrivate();
    }

    @Override
    public Duration getMaximumAge() {

        return LinkIDSDKConfigHolder.config().proto().maxTimeOffset();
    }
}
