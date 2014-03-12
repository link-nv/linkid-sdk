/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import javax.servlet.http.HttpServletRequest;
import org.opensaml.common.SAMLObject;


/**
 * <h2>{@link BindingUtil}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Apr 20, 2009</i> </p>
 *
 * @author lhunath
 */
public abstract class BindingUtil {

    /**
     * Parse a SAML object out of a request. This delegates to the SAML Binding specific method of parsing SAML objects.
     *
     * @param request HTTP Servlet Request
     *
     * @return optional relay state in the request or <code>null</code> if none.
     */
    public static String getRelayState(HttpServletRequest request) {

        if ("POST".equals( request.getMethod() ))
            // SAML HTTP Post Binding
            return PostBindingUtil.getRelayState( request );

        if ("GET".equals( request.getMethod() ))
            // SAML HTTP Redirect Binding
            return RedirectBindingUtil.getRelayState( request );

        // Not supported binding
        throw new UnsupportedOperationException( "Unsupported Binding, only HTTP Post or HTTP Redirect binding are supported" );
    }

    public static boolean isBreakFrame(HttpServletRequest request) {

        String breakFrameString = request.getParameter( "IsBreakFrame" );
        return null != breakFrameString && Boolean.parseBoolean( breakFrameString );
    }

    /**
     * Parse a SAML object out of a request. This delegates to the SAML Binding specific method of parsing SAML objects.
     *
     * @param request        HTTP Servlet Request
     * @param samlObjectType SAML Object class type
     *
     * @return the SAML Object or <code>null</code> if not found.
     */
    public static <T extends SAMLObject> T findSAMLObject(HttpServletRequest request, Class<T> samlObjectType) {

        SAMLObject samlObject;

        if ("POST".equals( request.getMethod() ))
            // SAML HTTP Post Binding
            samlObject = PostBindingUtil.getSAMLObject( request );
        else if ("GET".equals( request.getMethod() ))
            // SAML HTTP Redirect Binding
            samlObject = RedirectBindingUtil.getSAMLObject( request );
        else
            // Not supported binding
            throw new UnsupportedOperationException( "Unsupported Binding, only HTTP Post or HTTP Redirect binding are supported" );

        if (samlObject == null || !samlObjectType.isInstance( samlObject ))
            return null;

        return samlObjectType.cast( samlObject );
    }
}
