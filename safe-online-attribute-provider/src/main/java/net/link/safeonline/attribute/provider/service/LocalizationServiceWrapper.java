package net.link.safeonline.attribute.provider.service;

import static com.google.common.base.Preconditions.*;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import net.link.safeonline.attribute.provider.exception.LocalizationImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Attribute service wrapper, setting/unsetting the classloader for each Attribute Provider call.
 * <p/>
 * Also keeps a map of these "proxies".
 */
public class LocalizationServiceWrapper implements LocalizationService {

    static final Logger logger = LoggerFactory.getLogger( LocalizationServiceWrapper.class );

    private static final Map<LocalizationService, LocalizationServiceWrapper> proxyMap            = new WeakHashMap<LocalizationService, LocalizationServiceWrapper>();
    private static final ThreadLocal<ClassLoader>                             originalClassLoader = new ThreadLocal<ClassLoader>();
    private final transient WeakReference<LocalizationService> wrappedService;

    public LocalizationServiceWrapper(final LocalizationService wrappedService) {

        this.wrappedService = new WeakReference<LocalizationService>( wrappedService );
    }

    private LocalizationService getWrappedService() {

        return checkNotNull( checkNotNull( wrappedService, "Wrapped provider is no longer available!" ).get(), //
                "Wrapped provider is no longer available!" );
    }

    /**
     * @return The classloader of the wrapped attribute service.
     */
    private ClassLoader getProviderClassLoader() {

        return getWrappedService().getClass().getClassLoader();
    }

    /**
     * Activate the wrapped service's classloader on the thread and store the currently active classloader for {@link
     * #deactivateService()}.
     *
     * @return The given wrapped service, for call chaining.
     */
    private LocalizationService activateService() {

        if (originalClassLoader.get() != null) {
            logger.debug( "No wrapping needed, already done." );
            return getWrappedService();
        }

        checkState( originalClassLoader.get() == null, "Can't wrap: Already wrapped: %s.  Did we forget to unwrap somewhere?",
                originalClassLoader.get() );

        originalClassLoader.set( Thread.currentThread().getContextClassLoader() );
        Thread.currentThread().setContextClassLoader( getProviderClassLoader() );

        return getWrappedService();
    }

    /**
     * Restore the original classloader that was set aside by {@link #activateService()}.
     */
    private static void deactivateService() {

        if (originalClassLoader.get() == null) {
            logger.debug( "{} unwrap skipped, was not wrapped", Thread.currentThread() );
            return;
        }

        Thread.currentThread().setContextClassLoader( originalClassLoader.get() );
        originalClassLoader.remove();
    }

    /**
     * Get a classloader-managing wrapper for the given localization service.
     *
     * @param wrappedService A real attribute service implementation that may originate from a foreign classloader.
     *
     * @return A classloader-managing device factory wrapper.
     */
    public static LocalizationServiceWrapper of(final LocalizationService wrappedService) {

        LocalizationServiceWrapper proxyFactory = proxyMap.get( wrappedService );
        if (proxyFactory == null) {
            proxyMap.put( wrappedService, proxyFactory = new LocalizationServiceWrapper( wrappedService ) );
        }

        return proxyFactory;
    }

    public String findText(final String key, final Locale locale) {

        try {
            return activateService().findText( key, locale );
        }
        finally {
            deactivateService();
        }
    }

    public void importXML(final InputStream inputStream)
            throws LocalizationImportException {

        try {
            activateService().importXML( inputStream );
        }
        finally {
            deactivateService();
        }
    }
}
