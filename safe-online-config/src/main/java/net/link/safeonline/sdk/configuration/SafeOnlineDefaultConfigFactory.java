package net.link.safeonline.sdk.configuration;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import net.link.safeonline.keystore.AbstractFileBasedKeyStore;
import net.link.safeonline.keystore.AbstractResourceBasedKeyStore;
import net.link.safeonline.keystore.AbstractURLBasedKeyStore;
import net.link.safeonline.keystore.LinkIDKeyStore;
import net.link.util.config.DefaultConfigFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link SafeOnlineDefaultConfigFactory}<br> <sub>[in short] (TODO).</sub></h2>
 *
 * <p> <i>09 14, 2010</i> </p>
 *
 * @author lhunath
 */
public class SafeOnlineDefaultConfigFactory extends DefaultConfigFactory {

    public SafeOnlineDefaultConfigFactory() {

        super( "linkID" );
    }

    @Override
    protected String generateValueExtension(final Method method) {

        if ("appPath".equals( method.getName() ) && method.getDeclaringClass().equals( WebConfig.class ))
            if (getServletRequest() instanceof HttpServletRequest)
                return ((HttpServletRequest) getServletRequest()).getContextPath() + '/';
            else if (getServletRequest() == null)
                throw new UnsupportedOperationException( "Cannot generate appPath when servlet request is not set. "
                                                         + "Consider using the ConfigFilter in your web application or provide appPath as a property value." );
            else
                throw new UnsupportedOperationException( "Cannot generate appPath for non-HTTP request: " + getServletRequest() );

        if ("language".equals( method.getName() ) && method.getDeclaringClass().equals( LinkIDConfig.class ))
            if (getServletRequest() != null)
                return getServletRequest().getLocale().getLanguage();
            else
                throw new UnsupportedOperationException( "Cannot generate appPath when servlet request is not set. "
                                                         + "Consider using the ConfigFilter in your web application or provide appPath as a property value." );

        return null;
    }

    @Override
    protected <T> T toTypeExtension(final String value, final Class<T> type) {

        // KeyStores
        if (LinkIDKeyStore.class.isAssignableFrom( type )) {

            if (value.startsWith( "res:" ))
                return type.cast( new AbstractResourceBasedKeyStore( value.replaceFirst( "^res:", "" ) ) {
                } );
            if (value.startsWith( "url:" ))
                try {
                    return type.cast( new AbstractURLBasedKeyStore( new URL( value.replaceFirst( "^url:", "" ) ) ) {
                    } );
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException( e );
                }
            if (value.startsWith( "file:" ))
                return type.cast( new AbstractFileBasedKeyStore( new File( value.replaceFirst( "^file:", "" ) ) ) {
                } );
            if (value.startsWith( "class:" ))
                try {
                    return type.cast(
                            Thread.currentThread().getContextClassLoader().loadClass( value.replaceFirst( "^class:", "" ) ).newInstance() );
                }
                catch (InstantiationException e) {
                    throw new RuntimeException( e );
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException( e );
                }
                catch (ClassNotFoundException e) {
                    throw new RuntimeException( e );
                }
        }

        return null;
    }
}
