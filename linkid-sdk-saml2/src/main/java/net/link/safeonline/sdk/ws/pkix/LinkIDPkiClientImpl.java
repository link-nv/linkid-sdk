/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.pkix;

import net.link.util.logging.Logger;
import net.link.util.InternalInconsistencyException;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.ws.pki.LinkIDPkiClient;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.PEMParser;


/**
 * Implementation component for PKI client.
 *
 * @author fcorneli
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDPkiClientImpl implements LinkIDPkiClient {

    private static final Logger logger = Logger.get( LinkIDPkiClientImpl.class );

    private final String location;

    public LinkIDPkiClientImpl(String location) {

        this.location = location;
    }

    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    @Override
    public X509Certificate getCertificate() {

        HttpClient httpClient = new HttpClient();
        String uri = getUri();
        GetMethod getMethod = new GetMethod( uri );
        int statusCode;
        try {
            statusCode = httpClient.executeMethod( getMethod );
        }
        catch (HttpException e) {
            throw new InternalInconsistencyException( String.format( "HTTP error: %s", e.getMessage() ), e );
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( String.format( "IO error: %s", e.getMessage() ), e );
        }
        if (HttpServletResponse.SC_OK != statusCode)
            throw new InternalInconsistencyException( String.format( "Invalid status code: %s", statusCode ) );
        String responseBody;
        try {
            responseBody = getMethod.getResponseBodyAsString();
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( String.format( "IO error: %s", e.getMessage() ), e );
        }
        logger.dbg( "response body: %s", responseBody );
        StringReader stringReader = new StringReader( responseBody );
        Object obj;
        PEMParser pemReader = new PEMParser( stringReader );
        try {
            obj = pemReader.readObject();
        }
        catch (IOException e) {
            throw new InternalInconsistencyException( String.format( "IO error: %s", e.getMessage() ), e );
        }
        finally {
            IOUtils.closeQuietly( pemReader );
        }
        if (!(obj instanceof X509Certificate))
            throw new InternalInconsistencyException( String.format( "Invalid response type: %s", obj.getClass().getName() ) );
        return (X509Certificate) obj;
    }

    private String getUri() {

        return String.format( "http://%s/%s/pki-cert.pem", location, LinkIDSDKUtils.getSDKProperty( "linkid.auth.webapp.url" ) );
    }
}