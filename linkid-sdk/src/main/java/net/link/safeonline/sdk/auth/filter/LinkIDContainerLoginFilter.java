/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import net.link.util.logging.Logger;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;


/**
 * Servlet container filter that sets the servlet container user principal according to the SafeOnline authenticated user.
 * <p/>
 * <p>
 * The configuration of this filter should be managed via the <code>web.xml</code> deployment descriptor.
 * </p>
 *
 * @author fcorneli
 */
public class LinkIDContainerLoginFilter implements Filter {

    private static final Logger logger = Logger.get( LinkIDContainerLoginFilter.class );

    private static final String ALREADY_PROCESSED = LinkIDContainerLoginFilter.class.getName() + ".ALREADY_PROCESSED";

    public void init(FilterConfig config) {

        // empty
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (Boolean.TRUE.equals( request.getAttribute( ALREADY_PROCESSED ) )) {
            request.setAttribute( ALREADY_PROCESSED, Boolean.TRUE );

            String userId = LinkIDLoginManager.findUserId( httpRequest.getSession() );
            logger.dbg( "setting servlet container user principal to %s", userId );

            request = new LinkIDLoginHttpServletRequestWrapper( httpRequest, userId );
        }

        chain.doFilter( request, response );
    }

    public void destroy() {

        // empty
    }
}