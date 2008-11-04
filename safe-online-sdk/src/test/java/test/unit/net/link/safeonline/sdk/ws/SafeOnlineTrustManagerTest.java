/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.sdk.ws;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;

import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;


public class SafeOnlineTrustManagerTest {

    static final Log LOG = LogFactory.getLog(SafeOnlineTrustManagerTest.class);


    @Test
    public void trustAllSSLConnection()
            throws Exception {

        LOG.debug("trust all ssl connection");

        // setup
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyPair serverKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate serverCert = PkiTestUtils.generateSelfSignedCertificate(serverKeyPair, "CN=TestServer");
        KeyManager keyManager = new TestX509KeyManager(serverKeyPair, serverCert);
        sslContext.init(new KeyManager[] { keyManager }, null, null);
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        int port = getFreePort();
        final ServerSocket serverSocket = sslServerSocketFactory.createServerSocket(port);

        Thread serverThread = new Thread() {

            @Override
            public void run() {

                while (true) {
                    try {
                        LOG.debug("listening...");
                        Socket socket = serverSocket.accept();
                        LOG.debug("incomming connection");
                        byte[] input = new byte[512];
                        int size = socket.getInputStream().read(input);
                        LOG.debug("size: " + size);
                        String result = new String(input);
                        LOG.debug("result: " + result);
                        assertTrue(result.startsWith("hello world"));
                    } catch (IOException e) {
                        LOG.debug("IO error: " + e.getMessage(), e);
                    }
                }
            }
        };

        serverThread.start();

        SafeOnlineTrustManager.configureSsl();

        // operate: wrong server certificate
        X509Certificate foobarCert = PkiTestUtils.generateSelfSignedCertificate(serverKeyPair, "CN=FooBarServer");
        SafeOnlineTrustManager.setTrustedCertificate(foobarCert);
        SSLSocket socket = (SSLSocket) HttpsURLConnection.getDefaultSSLSocketFactory().createSocket("localhost", port);
        try {
            socket.getOutputStream().write("hello world".getBytes());
            fail();
        } catch (Exception e) {
            // expected
            LOG.debug("expected exception: " + e.getMessage());
            LOG.debug("exception type: " + e.getClass().getName());
        }
        socket.close();
        /*
         * Apparently we need to invalidate the SSL session to force a new SSL handshake.
         */
        SSLSession session = socket.getSession();
        session.invalidate();

        // operate: no trusted server cert configured
        SafeOnlineTrustManager.setTrustedCertificate(null);
        SSLSocket socketNoServerCert = (SSLSocket) HttpsURLConnection.getDefaultSSLSocketFactory().createSocket("localhost", port);
        socketNoServerCert.getOutputStream().write("hello world".getBytes());
        socketNoServerCert.close();
        SSLSession sessionNoServerCert = socketNoServerCert.getSession();
        sessionNoServerCert.invalidate();

        // operate: server cert configured
        SafeOnlineTrustManager.setTrustedCertificate(serverCert);
        SSLSocket socketServerCert = (SSLSocket) HttpsURLConnection.getDefaultSSLSocketFactory().createSocket("localhost", port);
        socketServerCert.getOutputStream().write("hello world".getBytes());
        socketServerCert.setKeepAlive(false);
        socketServerCert.close();

    }


    private static class TestX509KeyManager implements X509KeyManager {

        public static final String    SERVER_ALIAS = "test-server-alias";

        private final KeyPair         keyPair;

        private final X509Certificate certificate;


        public TestX509KeyManager(KeyPair keyPair, X509Certificate certificate) {

            this.keyPair = keyPair;
            this.certificate = certificate;
        }

        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {

            LOG.debug("chooseClientAlias");
            return null;
        }

        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {

            LOG.debug("chooseServerAlias; keyType: " + keyType);
            return SERVER_ALIAS;
        }

        public X509Certificate[] getCertificateChain(String alias) {

            LOG.debug("getCertificateChain: " + alias);
            if (SERVER_ALIAS.equals(alias))
                return new X509Certificate[] { this.certificate };
            return null;
        }

        public String[] getClientAliases(String keyType, Principal[] issuers) {

            LOG.debug("getClientAliases");
            return null;
        }

        public PrivateKey getPrivateKey(String alias) {

            LOG.debug("getPrivateKey: " + alias);
            if (SERVER_ALIAS.equals(alias)) {
                PrivateKey privateKey = this.keyPair.getPrivate();
                if (null == privateKey)
                    throw new SecurityException("no private key");
                return this.keyPair.getPrivate();
            }
            return null;
        }

        public String[] getServerAliases(String keyType, Principal[] issuers) {

            LOG.debug("getServerAliases; keyType: " + keyType);
            return null;
        }
    }


    private static int getFreePort()
            throws Exception {

        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }
}
