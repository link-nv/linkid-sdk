package net.link.safeonline.sdk.api.configuration;

import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import org.jetbrains.annotations.Nullable;
import org.joda.time.Duration;


/**
 * Created by wvdhaute
 * Date: 05/02/16
 * Time: 14:03
 */
public interface LinkIDConfigService {

    /**
     * @return the application's name. This name must remain constant and will only be used to identify the application in service
     * requests to linkID.
     */
    String name();

    /**
     * @return the linkID Base URL ( e.g. https://service.linkid.be )
     */
    String linkIDBase();

    /**
     * The maximum deviation in milliseconds between timestamps in WS-Security messages and the current system time.  This is used to
     * compensate for possible differences of the server and client's system clock.
     * <p/>
     * <i>[optional, default: 300000]</i>
     *
     * @return maximum deviation (ms) for WS-Security timestamps.
     */
    Duration maxTimeOffset();

    /**
     * @return The username that will provide the application's identification in the WS calls to linkID using the WS-Security Username token profile
     */
    @Nullable
    String username();

    /**
     * @return The password that will provide the application's identification in the WS calls to linkID using the WS-Security Username token profile
     */
    @Nullable
    String password();

    /**
     * e.g. CN=linkID Node linkID-localhost, OU=Development, L=SINT-MARTENS-LATEM, ST=VL, O=LIN.K_NV, C=BE
     *
     * @return The DN of the end certificate with which incoming messages should be signed if using the WS-Security X509 token profile. Return {@code null} if
     * not applicable.
     */
    X500Principal trustedDN();

    /**
     * Optional list of SSL certificates for certification. You can provide multiple for rollover purposes.
     * You can also return these via the KeyProvider. In that case linkID will look for all certificates under the alias @{link LinkIDConstants.SSL_ALIAS}
     *
     * @return the linkID SSL certificates.
     */
    @Nullable
    X509Certificate[] sslCertificates();
}
