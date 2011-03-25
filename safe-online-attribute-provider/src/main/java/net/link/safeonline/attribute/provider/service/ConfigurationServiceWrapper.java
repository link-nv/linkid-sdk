package net.link.safeonline.attribute.provider.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Attribute service wrapper, setting/unsetting the classloader for each Attribute Provider call.
 * <p/>
 * Also keeps a map of these "proxies".
 */
public class ConfigurationServiceWrapper implements ConfigurationService {

    static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceWrapper.class);

    private static final Map<ConfigurationService, ConfigurationServiceWrapper> proxyMap = new WeakHashMap<ConfigurationService, ConfigurationServiceWrapper>();
    private static final ThreadLocal<ClassLoader> originalClassLoader = new ThreadLocal<ClassLoader>();
    private final transient WeakReference<ConfigurationService> wrappedService;

    public ConfigurationServiceWrapper(final ConfigurationService wrappedService) {

        this.wrappedService = new WeakReference<ConfigurationService>(wrappedService);
    }

    public void initConfigurationValue(String group, String name, Object value) {

        try {
            activateService().initConfigurationValue(group, name, value);
        } finally {
            deactivateService();
        }
    }

    public Object getConfigurationValue(String group, String name, Object defaultValue) {

        try {
            return activateService().getConfigurationValue(group, name, defaultValue);
        } finally {
            deactivateService();
        }
    }

    private ConfigurationService getWrappedService() {

        return checkNotNull(checkNotNull(wrappedService, "Wrapped provider is no longer available!").get(), //
                "Wrapped provider is no longer available!");
    }

    /**
     * @return The classloader of the wrapped service.
     */
    public ClassLoader getProviderClassLoader() {

        return getWrappedService().getClass().getClassLoader();
    }

    /**
     * Activate the wrapped service's classloader on the thread and store the currently active classloader for {@link
     * #deactivateService()}.
     *
     * @return The given wrapped service, for call chaining.
     */
    private ConfigurationService activateService() {

        if (originalClassLoader.get() != null) {
            logger.debug("No wrapping needed, already done.");
            return getWrappedService();
        }

        checkState(originalClassLoader.get() == null, "Can't wrap: Already wrapped: %s.  Did we forget to unwrap somewhere?",
                originalClassLoader.get());

        originalClassLoader.set(Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(getProviderClassLoader());

        return getWrappedService();
    }

    /**
     * Restore the original classloader that was set aside by {@link #activateService()}.
     */
    private static void deactivateService() {

        if (originalClassLoader.get() == null) {
            logger.debug("{} unwrap skipped, was not wrapped", Thread.currentThread());
            return;
        }

        Thread.currentThread().setContextClassLoader(originalClassLoader.get());
        originalClassLoader.remove();
    }

    /**
     * Get a classloader-managing wrapper for the given service.
     *
     * @param wrappedService A real service implementation that may originate from a foreign classloader.
     * @return A classloader-managing service wrapper.
     */
    public static ConfigurationServiceWrapper of(final ConfigurationService wrappedService) {

        ConfigurationServiceWrapper proxyFactory = proxyMap.get(wrappedService);
        if (proxyFactory == null) {
            proxyMap.put(wrappedService, proxyFactory = new ConfigurationServiceWrapper(wrappedService));
        }

        return proxyFactory;
    }
}
