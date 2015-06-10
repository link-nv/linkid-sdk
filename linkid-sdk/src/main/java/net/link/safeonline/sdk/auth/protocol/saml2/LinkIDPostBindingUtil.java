/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import com.google.common.base.Charsets;
import net.link.util.logging.Logger;
import net.link.util.InternalInconsistencyException;
import java.io.IOException;
import java.security.KeyPair;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.*;
import net.link.util.common.CertificateChain;
import net.link.util.common.DomUtils;
import net.link.util.saml.SamlUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.JdkLogChute;
import org.bouncycastle.util.encoders.Base64;
import org.jetbrains.annotations.Nullable;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.security.*;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.MandatoryIssuerRule;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.security.SecurityException;


/**
 * Utility class for SAML2 authentication responses.
 *
 * @author wvdhaute
 */
public abstract class LinkIDPostBindingUtil {

    private static final Logger logger = Logger.get( LinkIDPostBindingUtil.class );

    /**
     * Sends a SAML2 authentication or logout Request using the specified Velocity template.
     *
     * @param samlRequest      SAML Request to send
     * @param signingKeyPair   keypair to sign request with
     * @param certificateChain optional certificate chain, if not specified KeyInfo in signature will be the PublicKey.
     * @param relayState       optional relay state
     * @param templateResource optional post binding velocity template resource
     * @param consumerUrl      URL of request consumer
     * @param response         HTTP Servlet Response
     * @param language         optional language
     *
     * @throws IOException IO Exception
     */
    @SuppressWarnings("UseOfPropertiesAsHashtable")
    public static void sendRequest(RequestAbstractType samlRequest, KeyPair signingKeyPair, CertificateChain certificateChain, String relayState,
                                   String templateResource, String consumerUrl, HttpServletResponse response, Locale language)
            throws IOException {

        logger.dbg( "sendRequest[HTTP POST] (RelayState: %s, To: %s)\n%s", relayState, consumerUrl,
                DomUtils.domToString( SamlUtils.marshall( samlRequest ), true ) );

        String encodedSamlRequestToken = new String(
                Base64.encode( DomUtils.domToString( SamlUtils.sign( samlRequest, signingKeyPair, certificateChain ) ).getBytes( Charsets.UTF_8 ) ),
                Charsets.UTF_8 );

        /*
         * We could use the opensaml2 HTTPPostEncoderBuilder here to construct the HTTP response. But this code is just too complex in
         * usage. It's easier to do all these things ourselves.
         */
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty( "resource.loader", "class" );
        velocityProperties.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, JdkLogChute.class.getName() );
        velocityProperties.setProperty( JdkLogChute.RUNTIME_LOG_JDK_LOGGER, LinkIDPostBindingUtil.class.getName() );
        velocityProperties.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        velocityProperties.setProperty( "file.resource.loader.cache ", "false" );
        VelocityEngine velocityEngine;
        try {
            velocityEngine = new VelocityEngine( velocityProperties );
            velocityEngine.init();
        }
        catch (Exception e) {
            throw new InternalInconsistencyException( "could not initialize velocity engine", e );
        }
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put( "action", consumerUrl );
        velocityContext.put( "SAMLRequest", encodedSamlRequestToken );
        if (null != relayState)
            velocityContext.put( "RelayState", relayState );
        if (null != language)
            velocityContext.put( LinkIDRequestConstants.LANGUAGE_REQUEST_PARAM, language.getLanguage() );

        Template template;
        //noinspection OverlyBroadCatchBlock
        try {
            template = velocityEngine.getTemplate( templateResource );
        }
        catch (Exception e) {
            throw new InternalInconsistencyException( String.format( "Velocity template error: %s", e.getMessage() ), e );
        }

        response.setContentType( "text/html; charset=UTF-8" );
        template.merge( velocityContext, response.getWriter() );
    }

    /**
     * Sends out a SAML response message to the specified consumer URL using SAML2 HTTP Post Binding.
     *
     * @param samlResponse     SAML Response to send
     * @param signingKeyPair   keypair to sign response with
     * @param certificateChain optional certificate chain, if not specified KeyInfo in signature will be the PublicKey.
     * @param relayState       optional relay state
     * @param templateResource optional post binding velocity template resource
     * @param consumerUrl      URL of request consumer
     * @param response         HTTP Servlet Response
     * @param language         optional language
     *
     * @throws IOException IO Exception
     */
    @SuppressWarnings("UseOfPropertiesAsHashtable")
    public static void sendResponse(StatusResponseType samlResponse, KeyPair signingKeyPair, CertificateChain certificateChain, String relayState,
                                    String templateResource, String consumerUrl, HttpServletResponse response, Locale language)
            throws IOException {

        logger.dbg( "sendResponse[HTTP POST] (RelayState: %s, To: %s)\n%s", relayState, consumerUrl,
                DomUtils.domToString( SamlUtils.marshall( samlResponse ), true ) );

        String encodedSamlResponseToken = new String(
                Base64.encode( DomUtils.domToString( SamlUtils.sign( samlResponse, signingKeyPair, certificateChain ) ).getBytes( Charsets.UTF_8 ) ),
                Charsets.UTF_8 );

        /*
         * We could use the opensaml2 HTTPPostEncoderBuilder here to construct the HTTP response. But this code is just too complex in
         * usage. It's easier to do all these things ourselves.
         */
        Properties velocityProperties = new Properties();
        velocityProperties.put( "resource.loader", "class" );
        velocityProperties.put( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, JdkLogChute.class.getName() );
        velocityProperties.put( JdkLogChute.RUNTIME_LOG_JDK_LOGGER, LinkIDPostBindingUtil.class.getName() );
        velocityProperties.put( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        VelocityEngine velocityEngine;
        try {
            velocityEngine = new VelocityEngine( velocityProperties );
            velocityEngine.init();
        }
        catch (Exception e) {
            throw new InternalInconsistencyException( "could not initialize velocity engine", e );
        }
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put( "action", consumerUrl );
        velocityContext.put( "SAMLResponse", encodedSamlResponseToken );
        if (null != relayState)
            velocityContext.put( "RelayState", relayState );
        if (null != language)
            velocityContext.put( LinkIDRequestConstants.LANGUAGE_REQUEST_PARAM, language.getLanguage() );

        Template template;
        //noinspection OverlyBroadCatchBlock
        try {
            template = velocityEngine.getTemplate( templateResource );
        }
        catch (Exception e) {
            throw new InternalInconsistencyException( String.format( "Velocity template error: %s", e.getMessage() ), e );
        }

        response.setContentType( "text/html; charset=UTF-8" );
        template.merge( velocityContext, response.getWriter() );
    }

    /**
     * @param request HTTP Servlet Request
     *
     * @return The {@link SAMLObject} that is in the given {@link HttpServletRequest} which is parsed as using the SAML HTTP POST Binding
     * protocol.<br> {@code null}: There is no SAML request or response.
     */
    @Nullable
    public static SAMLObject getSAMLObject(HttpServletRequest request) {

        // Check whether there is a SAML request or SAML response in the HTTP request.
        if (request.getParameter( "SAMLRequest" ) == null && request.getParameter( "SAMLResponse" ) == null)
            return null;

        BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
        messageContext.setInboundMessageTransport( new HttpServletRequestAdapter( request ) );

        messageContext.setSecurityPolicyResolver( new SecurityPolicyResolver() {

            @Override
            public Iterable<SecurityPolicy> resolve(MessageContext context) {

                return Collections.singletonList( resolveSingle( context ) );
            }

            @Override
            public SecurityPolicy resolveSingle(MessageContext context) {

                SecurityPolicy securityPolicy = new BasicSecurityPolicy();
                securityPolicy.getPolicyRules().add( new MandatoryIssuerRule() );

                return securityPolicy;
            }
        } );

        try {
            new HTTPPostDecoder().decode( messageContext );
        }
        catch (MessageDecodingException e) {
            throw new InternalInconsistencyException( "SAML message decoding error", e );
        }
        catch (SecurityPolicyException e) {
            throw new InternalInconsistencyException( "security policy error", e );
        }
        catch (SecurityException e) {
            throw new InternalInconsistencyException( "security error", e );
        }

        return messageContext.getInboundSAMLMessage();
    }

    public static String getRelayState(HttpServletRequest request) {

        return request.getParameter( "RelayState" );
    }
}
