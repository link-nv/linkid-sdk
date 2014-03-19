/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.configuration;

import net.link.util.logging.Logger;
import net.link.util.InternalInconsistencyException;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import org.jetbrains.annotations.Nullable;


/**
 * Enumeration of all supported authentication protocols.
 *
 * @author fcorneli
 */
public enum Protocol {

    SAML2( "net.link.safeonline.sdk.auth.protocol.saml2.Saml2ProtocolHandler" ),

    HAWS( "net.link.safeonline.sdk.auth.protocol.haws.HawsProtocolHandler" ),

    // does not have a protocol handler, has to be used directly using the WS client
    WS( null );

    private static final Logger logger = Logger.get( Protocol.class );

    private final String protocolHandlerClass;

    Protocol(final String protocolHandlerClass) {

        this.protocolHandlerClass = protocolHandlerClass;
    }

    public String getProtocolHandlerClass() {

        return protocolHandlerClass;
    }

    @Nullable
    public Object newHandler() {

        if (null == protocolHandlerClass)
            return null;

        try {
            Class<?> protocolHandler = getClass().getClassLoader().loadClass( protocolHandlerClass );

            return protocolHandler.getConstructor().newInstance();
        }
        catch (InstantiationException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (IllegalAccessException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (NoSuchMethodException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (InvocationTargetException e) {
            throw new InternalInconsistencyException( e );
        }
        catch (ClassNotFoundException e) {
            logger.err( "Protocol handler class %s not found.", protocolHandlerClass );
            return null;
        }
    }

    public static Protocol fromString(String text, Protocol fallback) {

        for (Protocol type : EnumSet.allOf( Protocol.class )) {
            if (type.toString().equalsIgnoreCase( text ))
                return type;
        }
        return fallback;
    }

}
