package net.link.safeonline.sdk.configuration;

import net.link.safeonline.keystore.LinkIDKeyStore;
import net.link.util.config.Config;


/**
 * <h2>{@link AppLinkIDConfig}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 17, 2010</i> </p>
 *
 * @author lhunath
 */
@Config.Group(prefix = "app")
public interface AppLinkIDConfig {

    /**
     * The application's internal name.  This name must remain constant and will only be used to identify the application in service
     * requests to linkID.
     *
     * <i>[required]</i>
     */
    @Config.Property(required = true)
    String name();

    /**
     * The keystore that will provide the application's identification, authentication and signing credentials, along with any certificates
     * that are used to verify messages from linkID services.
     *
     * <i>[required, default: res:application.jks]</i>
     */
    @Config.Property(required = true, unset = "res:application.jks")
    LinkIDKeyStore keyStore();

    /**
     * The password that protects the key store referenced by {@link AppLinkIDConfig#keyStore()}.
     *
     * <i>[required, default: secret]</i>
     */
    @Config.Property(required = true, unset = "secret")
    String keyStorePass();

    /**
     * The alias that identifies the private key entry from the key store referenced by  {@link AppLinkIDConfig#keyStore()} which the SDK
     * uses to obtain the application's identity and credentials.
     *
     * <i>[optional, default: Use the first key entry in the store]</i>
     */
    @Config.Property(required = false, unset = Config.Property.AUTO)
    String keyEntryAlias();

    /**
     * The password that protects the private key entry identified by {@link AppLinkIDConfig#keyEntryAlias()}.
     *
     * <i>[required, default: secret]</i>
     */
    @Config.Property(required = true, unset = "secret")
    String keyEntryPass();
}
