/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.exception.LinkIDValidationFailedException;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.auth.protocol.LinkIDAuthnProtocolRequestContext;
import net.link.safeonline.sdk.auth.protocol.LinkIDProtocolContext;
import net.link.safeonline.sdk.configuration.LinkIDSAMLBinding;
import net.link.safeonline.sdk.ws.LinkIDServiceFactory;
import net.link.util.common.CertificateChain;
import net.link.util.exception.ValidationFailedException;
import net.link.util.logging.Logger;
import net.link.util.saml.Saml2Utils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.saml2.core.Subject;


/**
 * Utility class for SAML2 responses.
 *
 * @author lhunath
 */
public abstract class LinkIDResponseUtil {

    private static final Logger logger = Logger.get( LinkIDResponseUtil.class );

    /**
     * Sends out a SAML response message to the specified consumer URL.
     *
     * @param consumerUrl          The URL of the SAML response message consumer.
     * @param certificateChain     optional certificate chain, if not specified KeyInfo in signature will be the PublicKey.
     * @param responseBinding      The SAML Binding to use for communicating the response to the consumer.
     * @param samlResponse         The SAML response token.
     * @param signingKeyPair       The {@link KeyPair} to use for generating the message signature.
     * @param relayState           The RelayState that was passed in the matching request.
     * @param response             The {@link HttpServletResponse} to write the response to.
     * @param postTemplateResource The resource that contains the template of the SAML HTTP POST Binding message.
     * @param language             A language hint to make the application retrieving the response use the same locale as the requesting
     *                             application.
     *
     * @throws IOException IO Exception
     */
    public static void sendResponse(String consumerUrl, LinkIDSAMLBinding responseBinding, StatusResponseType samlResponse, KeyPair signingKeyPair,
                                    CertificateChain certificateChain, HttpServletResponse response, String relayState, String postTemplateResource,
                                    @Nullable Locale language)
            throws IOException {

        switch (responseBinding) {
            case HTTP_POST:
                LinkIDPostBindingUtil.sendResponse( samlResponse, signingKeyPair, certificateChain, relayState, postTemplateResource, consumerUrl, response,
                        language );
                break;

            case HTTP_REDIRECT:
                LinkIDRedirectBindingUtil.sendResponse( samlResponse, signingKeyPair, relayState, consumerUrl, response );
                break;
        }
    }

    /**
     * Validates a SAML response in the specified HTTP request. Checks: <ul> <li>response ID</li> <li>response validated with STS WS
     * location</li> <li>at least 1 assertion present</li> <li>assertion subject</li> <li>assertion conditions notOnOrAfter and notBefore
     * </ul>
     *
     * @param request             HTTP Servlet Request
     * @param contexts            map of {@link LinkIDProtocolContext}'s, one matching the original authentication request will be looked up
     * @param trustedCertificates The linkID service certificates for validation of the HTTP-Redirect signature (else can be
     *                            {@code null} or empty).
     *                            If trusted certificates are specified and the binding is HTTP POST, the chain will be validated using the
     *                            linkID XKMS 2.0 WS.
     *
     * @return The SAML {@link LinkIDSaml2ResponseContext} that is in the HTTP request<br> {@code null} if there is no SAML message in the
     * HTTP request. Also contains (if present) the certificate chain embedded in the SAML {@link Response}'s signature.
     *
     * @throws ValidationFailedException validation failed for some reason
     */
    @Nullable
    public static LinkIDSaml2ResponseContext findAndValidateAuthnResponse(HttpServletRequest request, Map<String, LinkIDProtocolContext> contexts,
                                                                    Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {

        Response authnResponse = findAuthnResponse( request );
        if (authnResponse == null) {
            logger.dbg( "No Authn Response in request." );
            return null;
        }

        return validateAuthnResponse( authnResponse, request, contexts, trustedCertificates );
    }

    public static LinkIDSaml2ResponseContext validateAuthnResponse(final Response authnResponse, HttpServletRequest request, Map<String, LinkIDProtocolContext> contexts,
                                                             Collection<X509Certificate> trustedCertificates)
            throws ValidationFailedException {
        // Check whether the response is indeed a response to a previous request by comparing the InResponseTo fields
        LinkIDAuthnProtocolRequestContext authnRequest = (LinkIDAuthnProtocolRequestContext) contexts.get( authnResponse.getInResponseTo() );
        if (authnRequest == null || !authnResponse.getInResponseTo().equals( authnRequest.getId() )) {
            throw new ValidationFailedException( "Request's SAML response ID does not match that of any active requests." );
        }
        logger.dbg( "response matches request: %s", authnRequest );

        // validate signature
        CertificateChain certificateChain = Saml2Utils.validateSignature( authnResponse.getSignature(), request, trustedCertificates );

        if ((null == trustedCertificates || trustedCertificates.isEmpty()) && null != authnResponse.getSignature() && null != certificateChain) {
            // validate using XKMS v2.0
            try {
                LinkIDServiceFactory.getXkms2Client().validate( certificateChain.toArray() );
            }
            catch (LinkIDWSClientTransportException e) {
                throw new ValidationFailedException( String.format( "SAML2 Certificate chain validation failure: %s", e.getMessage() ), e );
            }
            catch (LinkIDValidationFailedException e) {
                throw new ValidationFailedException( String.format( "SAML2 Certificate chain validation failure: %s", e.getMessage() ), e );
            }
            catch (CertificateEncodingException e) {
                throw new ValidationFailedException( String.format( "SAML2 Certificate chain validation failure: %s", e.getMessage() ), e );
            }
        }

        // validate response
        validateResponse( authnResponse, authnRequest.getIssuer() );

        return new LinkIDSaml2ResponseContext( authnResponse, certificateChain );
    }

    /**
     * @param request HTTP Servlet Request
     *
     * @return The SAML {@link Response} that is in the HTTP request<br> {@code null} if there is no SAML message in the HTTP request.
     */
    public static Response findAuthnResponse(HttpServletRequest request) {

        return LinkIDBindingUtil.findSAMLObject( request, Response.class );
    }

    /**
     * Validate the SAML v2.0 Authentication Response.
     *
     * @param response the authentication response
     * @param audience the expected audience
     *
     * @throws ValidationFailedException validation failed for some reason
     */
    public static void validateResponse(Response response, @Nullable String audience)
            throws ValidationFailedException {

        DateTime now = new DateTime();

        // check status
        String samlStatusCode = response.getStatus().getStatusCode().getValue();
        if (!samlStatusCode.equals( StatusCode.AUTHN_FAILED_URI ) && !samlStatusCode.equals( StatusCode.UNKNOWN_PRINCIPAL_URI )
            && !StatusCode.SUCCESS_URI.equals( samlStatusCode )) {
            throw new ValidationFailedException( "Invalid SAML status code: " + samlStatusCode );
        }

        if (StatusCode.SUCCESS_URI.equals( samlStatusCode )) {
            List<Assertion> assertions = response.getAssertions();
            if (assertions.isEmpty()) {
                throw new ValidationFailedException( "missing Assertion in SAML2 Response" );
            }

            for (Assertion assertion : assertions) {
                validateAssertion( assertion, now, audience );
            }
        }
    }

    /**
     * Validates the specified assertion.
     * <p/>
     * Validates : <ul> <li>The notBefore and notOnOrAfter conditions based on the specified time.</li> <li>If the audience in the audience
     * restriction matches the specified audience</li> <li>If a subject is present</li> </ul>
     *
     * @param assertion        SAML v2.0 assertion to validate
     * @param now              current time to validate assertion's conditions against
     * @param expectedAudience expected audience in the assertion
     *
     * @throws ValidationFailedException One of the validation checks failed.
     */
    public static void validateAssertion(Assertion assertion, DateTime now, @Nullable String expectedAudience)
            throws ValidationFailedException {

        Conditions conditions = assertion.getConditions();
        DateTime notBefore = conditions.getNotBefore();
        DateTime notOnOrAfter = conditions.getNotOnOrAfter();

        logger.dbg( "now: %s", now.toString() );
        logger.dbg( "notBefore: %s", notBefore.toString() );
        logger.dbg( "notOnOrAfter : %s", notOnOrAfter.toString() );

        if (now.isBefore( notBefore )) {
            // time skew
            if (now.plusMinutes( 5 ).isBefore( notBefore ) || now.minusMinutes( 5 ).isAfter( notOnOrAfter )) {
                throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
            }
        } else if (now.isBefore( notBefore ) || now.isAfter( notOnOrAfter )) {
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : invalid SAML message timeframe" );
        }

        Subject subject = assertion.getSubject();
        if (null == subject) {
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing Assertion Subject" );
        }

        if (assertion.getAuthnStatements().isEmpty()) {
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnStatement" );
        }

        AuthnStatement authnStatement = assertion.getAuthnStatements().get( 0 );
        if (null == authnStatement.getAuthnContext()) {
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContext" );
        }

        if (null == authnStatement.getAuthnContext().getAuthnContextClassRef()) {
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : missing AuthnContextClassRef" );
        }

        if (expectedAudience != null) {
            validateAudienceRestriction( conditions, expectedAudience );
        }
    }

    /**
     * Check whether the audience of the response corresponds to the original audience restriction
     */
    private static void validateAudienceRestriction(Conditions conditions, String expectedAudience)
            throws ValidationFailedException {

        List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
        if (audienceRestrictions.isEmpty()) {
            throw new ValidationFailedException(
                    "SAML2 assertion validation audience=" + expectedAudience + " : no Audience Restrictions found in response assertion" );
        }

        AudienceRestriction audienceRestriction = audienceRestrictions.get( 0 );
        List<Audience> audiences = audienceRestriction.getAudiences();
        if (audiences.isEmpty()) {
            throw new ValidationFailedException( "SAML2 assertion validation audience=" + expectedAudience + " : no Audiences found in AudienceRestriction" );
        }

        Audience audience = audiences.get( 0 );
        if (!expectedAudience.equals( audience.getAudienceURI() )) {
            throw new ValidationFailedException(
                    "SAML2 assertion validation: audience name not correct, expected: " + expectedAudience + " was: " + audience.getAudienceURI() );
        }
    }
}