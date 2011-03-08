/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.auth.protocol.saml2;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.catalina.core.DummyRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link MockSAMLHttpServletRequest}</h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Apr 15, 2009</i> </p>
 *
 * @author lhunath
 */
public class MockSAMLHttpServletRequest extends HttpServletRequestWrapper {

    private static final Log LOG = LogFactory.getLog( MockSAMLHttpServletRequest.class );

    private final Map<String, String> parameters;

    private final StringBuffer requestURL;

    public MockSAMLHttpServletRequest(String queryString, StringBuffer requestURL) {

        super( new DummyRequest( "", requestURL.toString(), queryString ) );

        parameters = new HashMap<String, String>();
        this.requestURL = requestURL;

        try {
            LOG.debug( "querystring   : " + queryString );
            for (String queryParameter : queryString.split( "&" )) {
                LOG.debug( " - queryparam : " + queryParameter );

                String[] kv = queryParameter.split( "=", 2 );
                LOG.debug( " - - key      : " + kv[0] );
                LOG.debug( " - - value    : " + kv[1] );

                parameters.put( URLDecoder.decode( kv[0], "UTF-8" ), URLDecoder.decode( kv[1], "UTF-8" ) );
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException( "UTF-8 encoding not supported.", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer getRequestURL() {

        return requestURL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParameter(String name) {

        return parameters.get( name );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getParameterMap() {

        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getParameterNames() {

        return Collections.enumeration( parameters.keySet() );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getParameterValues(String name) {

        return new String[] { getParameter( name ) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod() {

        return "GET";
    }
}
