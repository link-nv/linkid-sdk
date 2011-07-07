package net.link.safeonline.attribute.provider;

import static com.google.common.base.Preconditions.*;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;
import net.link.safeonline.attribute.provider.exception.AttributeNotFoundException;
import net.link.safeonline.attribute.provider.service.LinkIDService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Attribute provider wrapper, setting/unsetting the classloader for each Attribute Provider call.
 * <p/>
 * Also keeps a map of these "proxies".
 */
public class AttributeProviderWrapper extends AttributeProvider {

    static final Logger logger = LoggerFactory.getLogger( AttributeProviderWrapper.class );

    private static final Map<AttributeProvider, AttributeProviderWrapper> proxyMap            = new WeakHashMap<AttributeProvider, AttributeProviderWrapper>();
    private static final ThreadLocal<ClassLoader>                         originalClassLoader = new ThreadLocal<ClassLoader>();
    private final transient WeakReference<AttributeProvider> wrappedProvider;

    public AttributeProviderWrapper(final AttributeProvider wrappedProvider) {

        this.wrappedProvider = new WeakReference<AttributeProvider>( wrappedProvider );
    }

    @Override
    public List<AttributeCore> listAttributes(LinkIDService linkIDService, String userId, String attributeName, boolean filterInvisible) {

        try {
            return activateProvider().listAttributes( linkIDService, userId, attributeName, filterInvisible );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public AttributeCore findAttribute(LinkIDService linkIDService, String userId, String attributeName, String attributeId) {

        try {
            return activateProvider().findAttribute( linkIDService, userId, attributeName, attributeId );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public AttributeCore findCompoundAttributeWhere(LinkIDService linkIDService, String userId, String parentAttributeName,
                                                    String memberAttributeName, Serializable memberValue) {

        try {
            return activateProvider().findCompoundAttributeWhere( linkIDService, userId, parentAttributeName, memberAttributeName,
                    memberValue );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public void removeAttributes(LinkIDService linkIDService, String userId, String attributeName) {

        try {
            activateProvider().removeAttributes( linkIDService, userId, attributeName );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public void removeAttribute(LinkIDService linkIDService, String userId, String attributeName, String attributeId)
            throws AttributeNotFoundException {

        try {
            activateProvider().removeAttribute( linkIDService, userId, attributeName, attributeId );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public void removeAttributes(LinkIDService linkIDService, String attributeName) {

        try {
            activateProvider().removeAttributes( linkIDService, attributeName );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public AttributeCore setAttribute(LinkIDService linkIDService, String userId, AttributeCore attribute) {

        try {
            return activateProvider().setAttribute( linkIDService, userId, attribute );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public List<AttributeType> getSupportedAttributeTypes() {

        try {
            return activateProvider().getSupportedAttributeTypes();
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public Map<Serializable, Long> categorize(LinkIDService linkIDService, List<String> subjects, String attributeName) {

        try {
            return activateProvider().categorize( linkIDService, subjects, attributeName );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public void intialize(LinkIDService linkIDService) {

        try {
            activateProvider().intialize( linkIDService );
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public AttributeProvider getAttributeProvider() {

        try {
            return activateProvider().getAttributeProvider();
        }
        finally {
            deactivateProvider();
        }
    }

    @Override
    public String getName() {

        try {
            return activateProvider().getName();
        }
        finally {
            deactivateProvider();
        }
    }

    private AttributeProvider getWrapperProvider() {

        return checkNotNull( checkNotNull( wrappedProvider, "Wrapped provider is no longer available!" ).get(), //
                "Wrapped provider is no longer available!" );
    }

    /**
     * @return The classloader of the wrapped attribute provider.
     */
    public ClassLoader getProviderClassLoader() {

        return getWrapperProvider().getClass().getClassLoader();
    }

    /**
     * Activate the wrapped provider's classloader on the thread and store the currently active classloader for {@link
     * #deactivateProvider()}.
     *
     * @return The given wrapped provider, for call chaining.
     */
    private AttributeProvider activateProvider() {

        if (originalClassLoader.get() != null) {
            logger.debug( "No wrapping needed, already done." );
            return getWrapperProvider();
        }

        checkState( originalClassLoader.get() == null, "Can't wrap: Already wrapped: %s.  Did we forget to unwrap somewhere?",
                originalClassLoader.get() );

        originalClassLoader.set( Thread.currentThread().getContextClassLoader() );
        Thread.currentThread().setContextClassLoader( getProviderClassLoader() );

        return getWrapperProvider();
    }

    /**
     * Restore the original classloader that was set aside by {@link #activateProvider()}.
     */
    private static void deactivateProvider() {

        if (originalClassLoader.get() == null) {
            logger.debug( "{} unwrap skipped, was not wrapped", Thread.currentThread() );
            return;
        }

        Thread.currentThread().setContextClassLoader( originalClassLoader.get() );
        originalClassLoader.remove();
    }

    /**
     * Get a classloader-managing wrapper for the given attribute provider.
     *
     * @param wrappedProvider A real attribute provider implementation that may originate from a foreign classloader.
     *
     * @return A classloader-managing device factory wrapper.
     */
    public static AttributeProviderWrapper of(final AttributeProvider wrappedProvider) {

        AttributeProviderWrapper proxyFactory = proxyMap.get( wrappedProvider );
        if (proxyFactory == null) {
            proxyMap.put( wrappedProvider, proxyFactory = new AttributeProviderWrapper( wrappedProvider ) );
        }

        return proxyFactory;
    }
}
