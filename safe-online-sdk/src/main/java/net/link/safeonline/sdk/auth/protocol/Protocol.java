/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol;

import java.lang.reflect.InvocationTargetException;
import net.link.safeonline.sdk.auth.protocol.openid.OpenIdProtocolHandler;
import net.link.safeonline.sdk.auth.protocol.saml2.Saml2ProtocolHandler;


/**
 * Enumeration of all supported authentication protocols.
 *
 * @author fcorneli
 */
public enum Protocol {

    SAML2( Saml2ProtocolHandler.class ),
    OPENID( OpenIdProtocolHandler.class );

    private final Class<? extends ProtocolHandler> protocolHandler;

    Protocol(Class<? extends ProtocolHandler> protocolHandler) {

        this.protocolHandler = protocolHandler;
    }

    public ProtocolHandler newHandler() {

        try {
            return protocolHandler.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException( e );
        } catch (IllegalAccessException e) {
            throw new RuntimeException( e );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException( e );
        } catch (InvocationTargetException e) {
            throw new RuntimeException( e );
        }
    }
}
