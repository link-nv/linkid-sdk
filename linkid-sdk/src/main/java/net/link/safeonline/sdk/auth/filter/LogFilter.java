/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.filter;

import net.link.util.logging.Logger;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;


/**
 * This filter gets the contents of the request and makes it available to subscribers in a StringBuffer object.
 *
 * @author wvdhaute
 */
public class LogFilter implements Filter {

    private static final Logger logger = Logger.get( LogFilter.class );

    @Override
    public void init(final FilterConfig filterConfig)
            throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        logger.dbg( "doFilter: %s", httpRequest.getRequestURL() );

        StringBuffer sb = new StringBuffer();

        sb.append( httpRequest.getMethod() )
          .append( " " )
          .append( httpRequest.getProtocol() )
          .append( " " )
          .append( httpRequest.getServerName() )
          .append( ":" )
          .append( httpRequest.getServerPort() )
          .append( httpRequest.getRequestURI() )
          .append( "\n" );
        sb.append( "Session attributes :\n" );
        Enumeration<?> sessionAttributes = httpRequest.getSession().getAttributeNames();
        while (sessionAttributes.hasMoreElements())
            sb.append( "  * " ).append( sessionAttributes.nextElement() ).append( "\n" );
        sb.append( "Request attributes :\n" );
        Enumeration<?> requestAttributes = httpRequest.getAttributeNames();
        while (requestAttributes.hasMoreElements())
            sb.append( "  * " ).append( requestAttributes.nextElement() ).append( "\n" );
        sb.append( "Request parameter names :\n" );
        Enumeration<?> requestParameters = httpRequest.getAttributeNames();
        while (requestParameters.hasMoreElements())
            sb.append( "  * " ).append( requestParameters.nextElement() ).append( "\n" );

        LogManager.getInstance().postLogBuffer( sb );

        chain.doFilter( request, response );
    }

    @Override
    public void destroy() {

    }
}
