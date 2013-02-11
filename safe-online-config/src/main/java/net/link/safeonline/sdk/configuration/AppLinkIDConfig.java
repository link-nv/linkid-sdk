package net.link.safeonline.sdk.configuration;

import javax.security.auth.x500.X500Principal;
import net.link.util.config.*;


/**
 * <h2>{@link AppLinkIDConfig}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
@Group(prefix = "app")
public interface AppLinkIDConfig {

    /**
     * The application's internal name.  This name must remain constant and will only be used to identify the application in service
     * requests to linkID.
     *
     * <i>[required]</i>
     */
    @Property(required = true)
    String name();

    /**
     * The key provider that will provide the application's identification, authentication and signing credentials, along with any certificates
     * that are used to validate messages from linkID services.
     *
     * <i>[required, default: classpath://application:secret:secret@application.jks]</i>
     */
    @Property(required = true, unset = "classpath://application:secret:secret@application.jks")
    KeyProvider keyProvider();

    /**
     * @return The DN of the end certificate with which incoming messages should be signed.
     */
    @Property(required = true, unset = "CN=linkID Node linkID-localhost, OU=Development, L=SINT-MARTENS-LATEM, ST=VL, O=LIN.K_NV, C=BE")
    X500Principal trustedDN();
}
