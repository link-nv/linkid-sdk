/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.TransformerException;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.keystore.PerformanceKeyStoreUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.test.util.DomTestUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.tidy.Tidy;


public class AuthenticationWebApplicationTest {

    static final Log LOG = LogFactory.getLog(AuthenticationWebApplicationTest.class);


    @Test
    public void testLogin() throws Exception {

        PrivateKeyEntry privateKeyEntry = PerformanceKeyStoreUtils.getPrivateKeyEntry();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
        PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

        Protocol.registerProtocol("https", new Protocol("https", new MySSLSocketFactory(), 443));

        HttpClient httpClient = new HttpClient();

        String uri = "https://localhost:8443/olas-auth/entry";

        PostMethod postMethod = new PostMethod(uri);
        postMethod.setRequestHeader("Cookie", "deflowered=true");

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        String authnRequest = AuthnRequestFactory.createAuthnRequest("performance-application", "performance-application", null, keyPair,
                "http://localhost:1234/performance-application", null, null, null, false);
        LOG.debug("authentication request: " + authnRequest);
        String encodedAuthnRequest = new String(Base64.encodeBase64(authnRequest.getBytes()));

        postMethod.addParameter(new NameValuePair("SAMLRequest", encodedAuthnRequest));

        int statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);

        String jsessionId = null;
        for (Cookie cookie : httpClient.getState().getCookies()) {
            LOG.debug("client side cookie: " + cookie);
            if ("JSESSIONID".equals(cookie.getName())) {
                jsessionId = cookie.getValue();
            }
        }
        assertNotNull(jsessionId);

        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        Header locationHeader = postMethod.getResponseHeader("Location");
        String location = locationHeader.getValue();
        LOG.debug("location: " + location);
        postMethod = new PostMethod(location);

        postMethod.addRequestHeader("Cookie", "JSESSIONID=" + jsessionId);
        statusCode = httpClient.executeMethod(postMethod);
        Tidy tidy = new Tidy();
        Document resultDocument = tidy.parseDOM(postMethod.getResponseBodyAsStream(), null);
        LOG.debug("result document: " + DomTestUtils.domToString(resultDocument));

        Node formNode = XPathAPI.selectSingleNode(resultDocument, "//form");
        assertNotNull(formNode);

        Node passwordInputNode = XPathAPI.selectSingleNode(formNode, "//input[@type='radio' and @value='password']");
        String passwordFieldName = passwordInputNode.getAttributes().getNamedItem("name").getNodeValue();
        String passwordFieldValue = passwordInputNode.getAttributes().getNamedItem("value").getNodeValue();
        LOG.debug("radio attribute: " + passwordFieldName + "=" + passwordFieldValue);

        postMethod = createFormPostMethod(formNode, jsessionId);
        postMethod.addParameter(new NameValuePair(passwordFieldName, passwordFieldValue));

        statusCode = httpClient.executeMethod(postMethod);
        LOG.debug("status code: " + statusCode);

        tidy = new Tidy();
        resultDocument = tidy.parseDOM(postMethod.getResponseBodyAsStream(), null);
        LOG.debug("result document: " + DomTestUtils.domToString(resultDocument));
    }

    private PostMethod createFormPostMethod(Node formNode, String jsessionId) throws TransformerException {

        NodeIterator hiddenInputNodeIterator = XPathAPI.selectNodeIterator(formNode, "//input[@type='hidden']");
        Node hiddenInputNode;
        List<NameValuePair> submitFields = new LinkedList<NameValuePair>();
        while (null != (hiddenInputNode = hiddenInputNodeIterator.nextNode())) {
            NamedNodeMap attributes = hiddenInputNode.getAttributes();
            String name = attributes.getNamedItem("name").getNodeValue();
            String value = attributes.getNamedItem("value").getNodeValue();
            LOG.debug("attribute: " + name + "=" + value);
            submitFields.add(new NameValuePair(name, value));
        }

        Node submitInputNode = XPathAPI.selectSingleNode(formNode, "//input[@type='submit']");
        String submitName = submitInputNode.getAttributes().getNamedItem("name").getNodeValue();
        submitFields.add(new NameValuePair(submitName, ""));

        String actionValue = formNode.getAttributes().getNamedItem("action").getNodeValue();
        LOG.debug("action value: " + actionValue);
        PostMethod postMethod = new PostMethod("https://localhost:8443" + actionValue);
        postMethod.addParameters(submitFields.toArray(new NameValuePair[] {}));

        postMethod.addRequestHeader("Cookie", "JSESSIONID=" + jsessionId);

        return postMethod;
    }


    public static class MySSLSocketFactory implements ProtocolSocketFactory {

        private final SSLSocketFactory sslSocketFactory;


        public MySSLSocketFactory() throws DriverException {

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                SecureRandom secureRandom = new SecureRandom();
                TrustManager trustManager = new MyTrustManager();
                TrustManager[] trustManagers = { trustManager };
                sslContext.init(null, trustManagers, secureRandom);
                this.sslSocketFactory = sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                throw new DriverException("no such algo");
            } catch (KeyManagementException e) {
                throw new DriverException("key error");
            }
        }

        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {

            LOG.debug("createSocket: " + host + ":" + port);
            return null;
        }

        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {

            LOG.debug("createSocket: " + host + ":" + port + ", local: " + localAddress + ":" + localPort);
            return null;
        }

        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params)
                                                                                                                               throws IOException,
                                                                                                                               UnknownHostException,
                                                                                                                               ConnectTimeoutException {

            LOG.debug("createSocket: " + host + ":" + port + ", local: " + localAddress + ":" + localPort + ", params: " + params);

            Socket socket = this.sslSocketFactory.createSocket(host, port, localAddress, localPort);
            return socket;
        }
    }

    static class MyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            throw new CertificateException("cannot verify client certificates");
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            if (null == chain) {
                throw new CertificateException("null certificate chain");
            }
            if (0 == chain.length) {
                throw new CertificateException("empty certificate chain");
            }
            if (null == authType) {
                throw new CertificateException("null authentication type");
            }
            if (0 == authType.length()) {
                throw new CertificateException("empty authentication type");
            }
            LOG.debug("server certificate: " + chain[0].getSubjectDN());
        }

        public X509Certificate[] getAcceptedIssuers() {

            return null;
        }
    }
}
