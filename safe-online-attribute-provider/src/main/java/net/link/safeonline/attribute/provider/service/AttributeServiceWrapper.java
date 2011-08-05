package net.link.safeonline.attribute.provider.service;

import static com.google.common.base.Preconditions.*;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.exception.AttributeTypeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Attribute service wrapper, setting/unsetting the classloader for each Attribute Provider call.
 * <p/>
 * Also keeps a map of these "proxies".
 */
public class AttributeServiceWrapper implements AttributeService {

    static final Logger logger = LoggerFactory.getLogger( AttributeServiceWrapper.class );

    private static final Map<AttributeService, AttributeServiceWrapper> proxyMap            = new WeakHashMap<AttributeService, AttributeServiceWrapper>();
    private static final ThreadLocal<ClassLoader>                       originalClassLoader = new ThreadLocal<ClassLoader>();
    private final transient WeakReference<AttributeService> wrappedService;

    public AttributeServiceWrapper(final AttributeService wrappedService) {

        this.wrappedService = new WeakReference<AttributeService>( wrappedService );
    }

    public List<AttributeCore> listAttributes(String userId, String attributeName, boolean filterInvisible)
            throws AttributeTypeNotFoundException {

        try {
            return activateService().listAttributes( userId, attributeName, filterInvisible );
        }
        finally {
            deactivateService();
        }
    }

    public AttributeCore findAttribute(String userId, String attributeName, String attributeId)
            throws AttributeTypeNotFoundException {

        try {
            return activateService().findAttribute( userId, attributeName, attributeId );
        }
        finally {
            deactivateService();
        }
    }

    public AttributeCore findCompoundAttributeWhere(String userId, String parentAttributeName, String memberAttributeName,
                                                    Serializable memberValue)
            throws AttributeTypeNotFoundException {

        try {
            return activateService().findCompoundAttributeWhere( userId, parentAttributeName, memberAttributeName, memberValue );
        }
        finally {
            deactivateService();
        }
    }

    private AttributeService getWrappedService() {

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
    private AttributeService activateService() {

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
     * Get a classloader-managing wrapper for the given attribute service.
     *
     * @param wrappedService A real attribute service implementation that may originate from a foreign classloader.
     *
     * @return A classloader-managing device factory wrapper.
     */
    public static AttributeServiceWrapper of(final AttributeService wrappedService) {

        AttributeServiceWrapper proxyFactory = proxyMap.get( wrappedService );
        if (proxyFactory == null) {
            proxyMap.put( wrappedService, proxyFactory = new AttributeServiceWrapper( wrappedService ) );
        }

        return proxyFactory;
    }
}
