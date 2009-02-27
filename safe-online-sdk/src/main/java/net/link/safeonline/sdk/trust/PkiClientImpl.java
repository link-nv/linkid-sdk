/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.trust;

import java.io.IOException;
import java.io.StringReader;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.openssl.PEMReader;


/**
 * Implementation component for PKI client.
 * 
 * @author fcorneli
 * 
 */
public class PkiClientImpl implements PkiClient {

    private static final Log LOG = LogFactory.getLog(PkiClientImpl.class);

    private final String     location;


    public PkiClientImpl(String location) {

        this.location = location;
    }

    public X509Certificate getCertificate() {

        HttpClient httpClient = new HttpClient();
        String uri = getUri();
        GetMethod getMethod = new GetMethod(uri);
        int statusCode;
        try {
            statusCode = httpClient.executeMethod(getMethod);
        } catch (HttpException e) {
            throw new RuntimeException("HTTP error: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        }
        if (HttpServletResponse.SC_OK != statusCode)
            throw new RuntimeException("invalid status code: " + statusCode);
        String responseBody;
        try {
            responseBody = getMethod.getResponseBodyAsString();
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        }
        LOG.debug("response body: " + responseBody);
        StringReader stringReader = new StringReader(responseBody);
        PEMReader pemReader = new PEMReader(stringReader);
        Object obj;
        try {
            obj = pemReader.readObject();
        } catch (IOException e) {
            throw new RuntimeException("IO error: " + e.getMessage(), e);
        }
        if (false == obj instanceof X509Certificate)
            throw new RuntimeException("invalid response type: " + obj.getClass().getName());
        X509Certificate certificate = (X509Certificate) obj;
        return certificate;
    }

    private String getUri() {

        return "http://" + location + "/olas-auth/pki/cert";
    }
}
