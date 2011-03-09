/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import net.link.safeonline.sdk.logging.ws.MessageTrackingHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;


/**
 * Abstract base implementation of the {@link WSClient} interface. Used by the different web service client components.
 *
 * @author fcorneli
 */
public abstract class AbstractWSClient implements WSClient {

    static final Log LOG = LogFactory.getLog( AbstractWSClient.class );

    protected final MessageTrackingHandler    messageLoggerHandler;
    private         Map<String, List<String>> responseHeaders;

    public AbstractWSClient() {

        messageLoggerHandler = new MessageTrackingHandler();
        responseHeaders = new HashMap<String, List<String>>();
    }

    /**
     * Registers the {@link X509TrustManager} for the specified port. If no {@link X509Certificate} was specified any server {@link
     * X509Certificate} will be accepted.
     */
    protected void registerTrustManager(Object port, final X509Certificate sslCertificate) {

        // Create TrustManager
        TrustManager[] trustManager = {
                new X509TrustManager() {

                    public X509Certificate[] getAcceptedIssuers() {

                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {

                        X509Certificate serverCertificate = chain[0];
                        LOG.debug( "server X509 subject: " + serverCertificate.getSubjectX500Principal().toString() );
                        LOG.debug( "authentication type: " + authType );
                        if (null == sslCertificate) {
                            LOG.warn( "No SSL certificate specified, accept any..." );
                            return;
                        }

                        try {
                            serverCertificate.verify( sslCertificate.getPublicKey() );
                            LOG.debug( "valid server certificate" );
                        } catch (InvalidKeyException e) {
                            throw new CertificateException( "Invalid Key" );
                        } catch (NoSuchAlgorithmException e) {
                            throw new CertificateException( "No such algorithm" );
                        } catch (NoSuchProviderException e) {
                            throw new CertificateException( "No such provider" );
                        } catch (SignatureException e) {
                            throw new CertificateException( "Wrong signature" );
                        }
                    }

                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {

                        throw new CertificateException( "this trust manager cannot be used as server-side trust manager" );
                    }
                } };

        // Create SSL Context
        try {
            SSLContext sslContext = SSLContext.getInstance( "TLS" );
            SecureRandom secureRandom = new SecureRandom();
            sslContext.init( null, trustManager, secureRandom );
            LOG.debug( "SSL context provider: " + sslContext.getProvider().getName() );

            // Setup TrustManager for validation
            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
            requestContext.put( JAXWSProperties.SSL_SOCKET_FACTORY, sslContext.getSocketFactory() );
        } catch (KeyManagementException e) {
            String msg = "key management error: " + e.getMessage();
            LOG.error( msg, e );
            throw new RuntimeException( msg, e );
        } catch (NoSuchAlgorithmException e) {
            String msg = "TLS algo not present: " + e.getMessage();
            LOG.error( msg, e );
            throw new RuntimeException( msg, e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public Document getInboundMessage() {

        return messageLoggerHandler.getInboundMessage();
    }

    /**
     * {@inheritDoc}
     */
    public Document getOutboundMessage() {

        return messageLoggerHandler.getOutboundMessage();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCaptureMessages() {

        return messageLoggerHandler.isCaptureMessages();
    }

    /**
     * {@inheritDoc}
     */
    public void setCaptureMessages(boolean captureMessages) {

        messageLoggerHandler.setCaptureMessages( captureMessages );
    }

    /**
     * Registers the SOAP handler that this instance manages on the given JAX-WS port component.
     */
    protected void registerMessageLoggerHandler(Object port) {

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add( messageLoggerHandler );
        binding.setHandlerChain( handlerChain );
    }

    /**
     * Call this method after your service request to set the response context of the response.<br> <br> For example:<br> <code>finally {
     * retrieveHeadersFromPort(this.port); }</code>
     */
    @SuppressWarnings("unchecked")
    protected void retrieveHeadersFromPort(Object port) {

        if (!(port instanceof BindingProvider))
            throw new IllegalArgumentException( "Can only retrieve result HTTP headers from a JAX-WS proxy object." );

        Map<String, Object> context = ((BindingProvider) port).getResponseContext();
        if (context == null || !context.containsKey( MessageContext.HTTP_RESPONSE_HEADERS ))
            return;

        responseHeaders = (Map<String, List<String>>) context.get( MessageContext.HTTP_RESPONSE_HEADERS );
    }

    /**
     * Deprecated profiling exception wrapper.
     *
     * Call this method when your service request failed with a ProfiledException. This will extract the profile headers from the
     * exception..<br> <br> For example:<br> <code>catch (ProfiledException e) { throw retrieveHeadersFromException(e); }</code>
     */
    @Deprecated
    protected RuntimeException retrieveHeadersFromException(Exception e) {

        Throwable cause = e;

        //        if (e instanceof ProfiledException) {
        //            for (Map.Entry<String, String> header : ((ProfiledException) e).getHeaders().entrySet())
        //                responseHeaders.put( header.getKey(), Arrays.asList( new String[] { header.getValue() } ) );
        //
        //            // Throw the exception wrapped in the ProfiledException.
        //            cause = e.getCause();
        //        }

        if (cause instanceof RuntimeException)
            return (RuntimeException) cause;
        return new RuntimeException( cause );
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getHeaders() {

        return responseHeaders;
    }
}
