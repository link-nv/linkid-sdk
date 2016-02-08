package net.link.safeonline.sdk.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.security.auth.x500.X500Principal;
import net.link.safeonline.sdk.api.configuration.LinkIDConfigService;
import net.link.util.InternalInconsistencyException;
import org.jetbrains.annotations.Nullable;
import org.joda.time.Duration;


/**
 * Default linkID config implementation. Looks for a linkID.properties file in the classpath.
 * <p/>
 * If you are using the X509 profile, you pass along the keyProvider separately.
 * See: ResourceKeyStoreKeyProvider, URLKeyStoreKeyProvider, FileKeyStoreKeyProvider for examples
 * <p/>
 * Created by wvdhaute
 * Date: 05/02/16
 * Time: 14:06
 */
public class LinkIDConfig implements LinkIDConfigService {

    private final String        name;
    private final String        linkIDBase;
    //
    private final Duration      maxTimeOffset;
    //
    private final String        username;
    private final String        password;
    //
    private final X500Principal trustedDN;


    private static class LinkIDDefaultConfigHolder {

        private static LinkIDConfigService config = new LinkIDConfig();

        public static void reload() {

            config = new LinkIDConfig();
        }
    }

    public static LinkIDConfigService get() {

        return LinkIDDefaultConfigHolder.config;
    }

    private Properties getProperties()
            throws IOException {

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream( getPropertiesFileName() );
        Properties properties = new Properties();
        properties.load( inputStream );
        return properties;
    }

    private LinkIDConfig() {

        try {

            Properties props = getProperties();

            name = props.getProperty( "linkid.config.name" );
            linkIDBase = props.getProperty( "linkid.config.linkIDBase" );

            String maxTimeOffsetString = props.getProperty( "linkid.config.maxTimeOffset" );
            maxTimeOffset = null != maxTimeOffsetString? Duration.parse( maxTimeOffsetString ): new Duration( 300000 );

            username = props.getProperty( "linkid.config.username" );
            password = props.getProperty( "linkid.config.password" );

            String trustedDNString = props.getProperty( "linkid.config.trustedDN" );
            trustedDN = null != trustedDNString? new X500Principal( trustedDNString )
                    : new X500Principal( "CN=linkID Node linkID-localhost, OU=Development, L=SINT-MARTENS-LATEM, ST=VL, O=LIN.K_NV, C=BE" );

        }
        catch (IOException | NumberFormatException e) {

            throw new InternalInconsistencyException( e );
        }
    }

    /**
     * @return the name of the properties file, override to use another
     */
    protected String getPropertiesFileName() {

        return "linkID.properties";
    }

    // Accessors

    @Override
    public String name() {

        return name;
    }

    @Override
    public String linkIDBase() {

        return linkIDBase;
    }

    @Override
    public Duration maxTimeOffset() {

        return maxTimeOffset;
    }

    @Nullable
    @Override
    public String username() {

        return username;
    }

    @Nullable
    @Override
    public String password() {

        return password;
    }

    @Override
    public X500Principal trustedDN() {

        return trustedDN;
    }

    @Nullable
    @Override
    public X509Certificate[] sslCertificates() {

        return null;
    }
}
