package net.link.safeonline.sdk.ws;

import static com.lyndir.lhunath.opal.system.util.ObjectUtils.*;

import be.fedict.trust.TrustValidator;
import be.fedict.trust.linker.TrustLinkerResultException;
import be.fedict.trust.repository.MemoryCertificateRepository;
import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.util.ObjectUtils;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import javax.security.auth.x500.X500Principal;
import net.link.safeonline.sdk.configuration.SDKConfigHolder;
import net.link.util.common.CertificateChain;
import net.link.util.common.LazyPublicKeyTrustLinker;
import net.link.util.config.KeyProvider;
import net.link.util.ws.security.AbstractWSSecurityConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.Duration;


/**
 * <h2>{@link SDKWSSecurityConfiguration}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>03 31, 2011</i> </p>
 *
 * @author lhunath
 */
public class SDKWSSecurityConfiguration extends AbstractWSSecurityConfiguration {

    static final Logger logger = Logger.get( SDKWSSecurityConfiguration.class );

    private final X500Principal trustedDN;
    private final KeyProvider   keyProvider;

    public SDKWSSecurityConfiguration() {

        this( null, null );
    }

    public SDKWSSecurityConfiguration(@Nullable final X500Principal trustedDN, @Nullable final KeyProvider keyProvider) {

        this.trustedDN = trustedDN;
        this.keyProvider = keyProvider;
    }

    public boolean isCertificateChainTrusted(final CertificateChain aCertificateChain) {

        // Manually check whether the end certificate has the correct DN.
        if (!ObjectUtils.isEqual( aCertificateChain.getIdentityCertificate().getSubjectX500Principal(), getTrustedDN() ))
            return false;

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

        return ObjectUtils.ifNotNullElse( trustedDN, SDKConfigHolder.config().linkID().app().trustedDN() );
    }

    public KeyProvider getKeyProvider() {

        return ifNotNullElse( keyProvider, new NNSupplier<KeyProvider>() {
            @NotNull
            public KeyProvider get() {

                return SDKConfigHolder.config().linkID().app().keyProvider();
            }
        } );
    }

    public CertificateChain getIdentityCertificateChain() {

        return getKeyProvider().getIdentityCertificateChain();
    }

    public PrivateKey getPrivateKey() {

        return getKeyProvider().getIdentityKeyPair().getPrivate();
    }

    @Override
    public Duration getMaximumAge() {

        return SDKConfigHolder.config().proto().maxTimeOffset();
    }
}
