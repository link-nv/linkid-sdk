package net.link.safeonline.sdk.configuration;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import net.link.util.config.DefaultConfigFactory;


/**
 * <h2>{@link SafeOnlineDefaultConfigFactory}<br> <sub>[in short].</sub></h2>
 * <p/>
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
}
