/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This filter gets the contents of the request and makes it available to subscribers in a StringBuffer object.
 *
 * @author wvdhaute
 */
public class LogFilter implements Filter {

    private static final Log LOG = LogFactory.getLog( LogFilter.class );

    public void destroy() {

        LOG.debug( "destroy" );
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        LOG.debug( "doFilter: " + httpRequest.getRequestURL() );

        StringBuffer sb = new StringBuffer();

        sb.append( httpRequest.getMethod() + " " + httpRequest.getProtocol() + " " + httpRequest.getServerName() + ":"
                   + httpRequest.getServerPort() + httpRequest.getRequestURI() + "\n" );
        sb.append( "Session attributes :\n" );
        Enumeration<?> sessionAttributes = httpRequest.getSession().getAttributeNames();
        while (sessionAttributes.hasMoreElements())
            sb.append( "  * " + sessionAttributes.nextElement() + "\n" );
        sb.append( "Request attributes :\n" );
        Enumeration<?> requestAttributes = httpRequest.getAttributeNames();
        while (requestAttributes.hasMoreElements())
            sb.append( "  * " + requestAttributes.nextElement() + "\n" );
        sb.append( "Request parameter names :\n" );
        Enumeration<?> requestParameters = httpRequest.getAttributeNames();
        while (requestParameters.hasMoreElements())
            sb.append( "  * " + requestParameters.nextElement() + "\n" );

        LogManager.getInstance().postLogBuffer( sb );

        chain.doFilter( request, response );
    }

    public void init(FilterConfig config) {

        LOG.debug( "init" );
    }
}
