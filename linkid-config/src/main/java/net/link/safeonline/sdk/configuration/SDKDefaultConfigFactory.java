/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import net.link.util.config.DefaultConfigFactory;
import org.jetbrains.annotations.NotNull;


/**
 * <h2>{@link SDKDefaultConfigFactory}<br> <sub>[in short].</sub></h2>
 * <p/>
 * <p> <i>09 14, 2010</i> </p>
 *
 * @author lhunath
 */
public class SDKDefaultConfigFactory extends DefaultConfigFactory {

    public SDKDefaultConfigFactory() {

        super( "linkID" );
    }

    @Override
    protected String generateValueExtension(@NotNull final Method method) {

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
