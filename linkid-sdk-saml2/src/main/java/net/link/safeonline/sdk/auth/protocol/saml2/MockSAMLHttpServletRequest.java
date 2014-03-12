/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import net.link.util.logging.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import javax.servlet.http.HttpServletRequestWrapper;
import net.link.util.common.DummyServletRequest;


/**
 * <h2>{@link MockSAMLHttpServletRequest}</h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Apr 15, 2009</i> </p>
 *
 * @author lhunath
 */
public class MockSAMLHttpServletRequest extends HttpServletRequestWrapper {

    private static final Logger logger = Logger.get( MockSAMLHttpServletRequest.class );

    private final Map<String, String> parameters;

    private final StringBuffer requestURL;

    public MockSAMLHttpServletRequest(final String queryString, StringBuffer requestURL) {

        //        super( new DummyServletRequest("", requestURL.toString(), queryString )));

        //        super( new DummyRequest( "", requestURL.toString(), queryString ) );
        super( new DummyServletRequest() {

            @Override
            public String getQueryString() {

                return queryString;
            }
        } );

        parameters = new HashMap<String, String>();
        this.requestURL = requestURL;

        try {
            logger.dbg( "querystring  %s : ", queryString );
            for (String queryParameter : queryString.split( "&" )) {
                logger.dbg( " - queryparam : %s", queryParameter );

                String[] kv = queryParameter.split( "=", 2 );
                logger.dbg( " - - key      : %s", kv[0] );
                logger.dbg( " - - value    : %s", kv[1] );

                parameters.put( URLDecoder.decode( kv[0], "UTF-8" ), URLDecoder.decode( kv[1], "UTF-8" ) );
            }
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException( "UTF-8 encoding not supported.", e );
        }
    }

    @Override
    public StringBuffer getRequestURL() {

        return requestURL;
    }

    @Override
    public String getParameter(String name) {

        return parameters.get( name );
    }

    @Override
    public Map<String, String> getParameterMap() {

        return parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {

        return Collections.enumeration( parameters.keySet() );
    }

    @Override
    public String[] getParameterValues(String name) {

        return new String[] { getParameter( name ) };
    }

    @Override
    public String getMethod() {

        return "GET";
    }
}
