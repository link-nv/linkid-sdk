package net.link.safeonline.sdk.configuration;

import net.link.util.config.AppConfig;
import net.link.util.config.Config;
import net.link.util.config.ConfigFilter;
import net.link.util.config.DefaultConfigFactory;

/**
 * <h2>{@link SafeOnlineConfigFilter}<br>
 * <sub>A filter that provides some non-default {@link Config} behaviour.</sub></h2>
 *
 * <p>
 * Any application that wants {@link DefaultConfigFactory} to gain access to their web.xml for configuration parameters needs to use this filter.
 * The filter will make the servlet context available the default config from within the current thread, allowing it to gain access to the context parameters contained within.
 * </p>
 *
 * <p>
 * The filter also provides a reliable way for an application to provide their own {@link Config} and/or {@link AppConfig} implementation.
 * The filter will set the configs it was created with as active within the current thread for each request that goes through it.
 * </p>
 *
 * <p>
 * <i>09 15, 2010</i>
 * </p>
 *
 * @author lhunath
 */
public class SafeOnlineConfigFilter extends ConfigFilter {

    public SafeOnlineConfigFilter() {

        super( new SafeOnlineConfigHolder() );
    }

    protected SafeOnlineConfigFilter(SafeOnlineConfigHolder configHolder) {

        super( configHolder );
    }
}
