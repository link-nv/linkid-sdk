package net.link.safeonline.sdk.ws;

import java.security.cert.X509Certificate;
import java.util.ResourceBundle;
import javax.xml.ws.BindingProvider;
import net.link.util.ws.AbstractWSClient;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 16/02/16
 * Time: 13:14
 */
public abstract class LinkIDAbstractWSClient<P> extends AbstractWSClient<P> {

    protected LinkIDAbstractWSClient(final String location, final P port, @Nullable final X509Certificate[] sslCertificates) {

        super( port, sslCertificates );

        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, String.format( "%s/%s", location, getSDKProperty( getLocationProperty() ) ) );

    }

    private static String getSDKProperty(final String key) {

        ResourceBundle properties = ResourceBundle.getBundle( "sdk_config" );
        return properties.getString( key );
    }

    // Abstract methods

    protected abstract String getLocationProperty();
}
