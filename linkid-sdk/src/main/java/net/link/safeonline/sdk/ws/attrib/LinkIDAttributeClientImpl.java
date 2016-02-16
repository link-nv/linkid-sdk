/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.attrib;

import com.sun.xml.ws.client.ClientTransportException;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.attribute.LinkIDCompound;
import net.link.safeonline.sdk.api.exception.LinkIDAttributeNotFoundException;
import net.link.safeonline.sdk.api.exception.LinkIDAttributeUnavailableException;
import net.link.safeonline.sdk.api.exception.LinkIDRequestDeniedException;
import net.link.safeonline.sdk.api.exception.LinkIDSubjectNotFoundException;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.ws.LinkIDSamlpSecondLevelErrorCode;
import net.link.safeonline.sdk.api.ws.LinkIDSamlpTopLevelErrorCode;
import net.link.safeonline.sdk.api.ws.LinkIDWebServiceConstants;
import net.link.safeonline.sdk.api.ws.attrib.LinkIDAttributeClient;
import net.link.safeonline.sdk.ws.LinkIDAbstractWSClient;
import net.link.safeonline.ws.attrib.LinkIDSAMLAttributeServiceFactory;
import net.link.util.InternalInconsistencyException;
import net.link.util.logging.Logger;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import oasis.names.tc.saml._2_0.assertion.AssertionType;
import oasis.names.tc.saml._2_0.assertion.AttributeStatementType;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.assertion.StatementAbstractType;
import oasis.names.tc.saml._2_0.assertion.SubjectType;
import oasis.names.tc.saml._2_0.protocol.AttributeQueryType;
import oasis.names.tc.saml._2_0.protocol.ResponseType;
import oasis.names.tc.saml._2_0.protocol.SAMLAttributePort;
import oasis.names.tc.saml._2_0.protocol.StatusCodeType;
import oasis.names.tc.saml._2_0.protocol.StatusType;
import org.jetbrains.annotations.Nullable;


/**
 * Implementation of attribute client. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class LinkIDAttributeClientImpl extends LinkIDAbstractWSClient<SAMLAttributePort> implements LinkIDAttributeClient {

    static final Logger logger = Logger.get( LinkIDAttributeClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDAttributeClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDAttributeClientImpl(final String location, final X509Certificate[] sslCertificates,
                                     final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDAttributeClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( location, LinkIDSAMLAttributeServiceFactory.newInstance().getSAMLAttributePort(), sslCertificates );
    }

    @Override
    protected String getLocationProperty() {

        return "linkid.ws.attribute.path";
    }

    private ResponseType getResponse(AttributeQueryType request)
            throws LinkIDWSClientTransportException {

        try {
            return getPort().attributeQuery( request );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }
    }

    private static void validateStatus(ResponseType response)
            throws LinkIDAttributeNotFoundException, LinkIDRequestDeniedException, LinkIDAttributeUnavailableException, LinkIDSubjectNotFoundException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        LinkIDSamlpTopLevelErrorCode linkIDSamlpTopLevelErrorCode = LinkIDSamlpTopLevelErrorCode.getSamlpTopLevelErrorCode( statusCodeValue );
        if (LinkIDSamlpTopLevelErrorCode.SUCCESS != linkIDSamlpTopLevelErrorCode) {
            StatusCodeType secondLevelStatusCode = statusCode.getStatusCode();
            if (null != secondLevelStatusCode) {
                String secondLevelStatusCodeValue = secondLevelStatusCode.getValue();
                LinkIDSamlpSecondLevelErrorCode linkIDSamlpSecondLevelErrorCode = LinkIDSamlpSecondLevelErrorCode.getSamlpTopLevelErrorCode(
                        secondLevelStatusCodeValue );

                if (LinkIDSamlpSecondLevelErrorCode.INVALID_ATTRIBUTE_NAME_OR_VALUE == linkIDSamlpSecondLevelErrorCode) {
                    throw new LinkIDAttributeNotFoundException();
                }

                if (LinkIDSamlpSecondLevelErrorCode.REQUEST_DENIED == linkIDSamlpSecondLevelErrorCode) {
                    throw new LinkIDRequestDeniedException();
                }

                if (LinkIDSamlpSecondLevelErrorCode.ATTRIBUTE_UNAVAILABLE == linkIDSamlpSecondLevelErrorCode) {
                    throw new LinkIDAttributeUnavailableException();
                }

                if (LinkIDSamlpSecondLevelErrorCode.UNKNOWN_PRINCIPAL == linkIDSamlpSecondLevelErrorCode) {
                    throw new LinkIDSubjectNotFoundException();
                }

                logger.dbg( "second level status code: %s", secondLevelStatusCode.getValue() );
            }
            throw new InternalInconsistencyException( String.format( "error: %s", statusCodeValue ) );
        }
    }

    private static AttributeQueryType getAttributeQuery(String userId, String attributeName) {

        Set<String> attributeNames = Collections.singleton( attributeName );
        return getAttributeQuery( userId, attributeNames );
    }

    private static AttributeQueryType getAttributeQuery(String userId, Set<String> attributeNames) {

        ObjectFactory samlObjectFactory = new ObjectFactory();
        AttributeQueryType attributeQuery = new AttributeQueryType();
        SubjectType subject = new SubjectType();
        NameIDType subjectName = new NameIDType();
        subjectName.setValue( userId );
        subject.getContent().add( samlObjectFactory.createNameID( subjectName ) );
        attributeQuery.setSubject( subject );

        List<AttributeType> attributes = attributeQuery.getAttribute();
        for (String attributeName : attributeNames) {
            AttributeType attribute = new AttributeType();
            attribute.setName( attributeName );
            attributes.add( attribute );
        }
        return attributeQuery;
    }

    private static AttributeQueryType getAttributeQuery(String userId, Map<String, List<LinkIDAttribute<Serializable>>> attributes) {

        Set<String> attributeNames = attributes.keySet();
        return getAttributeQuery( userId, attributeNames );
    }

    @Override
    public void getAttributes(String userId, Map<String, List<LinkIDAttribute<Serializable>>> attributes)
            throws LinkIDAttributeNotFoundException, LinkIDRequestDeniedException, LinkIDWSClientTransportException, LinkIDAttributeUnavailableException,
                   LinkIDSubjectNotFoundException {

        AttributeQueryType request = getAttributeQuery( userId, attributes );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
    }

    private static void getAttributeValues(ResponseType response, Map<String, List<LinkIDAttribute<Serializable>>> attributeMap) {

        List<Serializable> assertions = response.getAssertionOrEncryptedAssertion();
        if (assertions.isEmpty()) {
            throw new InternalInconsistencyException( "No assertions in response" );
        }
        AssertionType assertion = (AssertionType) assertions.get( 0 );

        List<StatementAbstractType> statements = assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement();
        if (statements.isEmpty()) {
            throw new InternalInconsistencyException( "No statements in response assertion" );
        }
        AttributeStatementType attributeStatement = (AttributeStatementType) statements.get( 0 );

        for (Object attributeTypeObject : attributeStatement.getAttributeOrEncryptedAttribute()) {
            AttributeType attributeType = (AttributeType) attributeTypeObject;

            LinkIDAttribute<Serializable> attribute = getAttributeSDK( attributeType );

            List<LinkIDAttribute<Serializable>> attributes = attributeMap.get( attribute.getName() );
            if (null == attributes) {
                attributes = new LinkedList<LinkIDAttribute<Serializable>>();
            }
            attributes.add( attribute );
            attributeMap.put( attribute.getName(), attributes );
        }
    }

    private static LinkIDAttribute<Serializable> getAttributeSDK(AttributeType attributeType) {

        String attributeId = findAttributeId( attributeType );
        LinkIDAttribute<Serializable> attribute = new LinkIDAttribute<Serializable>( attributeId, attributeType.getName(), null );

        List<Object> attributeValues = attributeType.getAttributeValue();
        if (attributeValues.isEmpty()) {
            return attribute;
        }

        if (attributeType.getAttributeValue().get( 0 ) instanceof AttributeType) {

            AttributeType compoundValueAttribute = (AttributeType) attributeType.getAttributeValue().get( 0 );

            // compound
            List<LinkIDAttribute<?>> compoundMembers = new LinkedList<LinkIDAttribute<?>>();
            for (Object memberAttributeObject : compoundValueAttribute.getAttributeValue()) {

                AttributeType memberAttribute = (AttributeType) memberAttributeObject;
                LinkIDAttribute<Serializable> member = new LinkIDAttribute<Serializable>( attributeId, memberAttribute.getName(), null );
                if (!memberAttribute.getAttributeValue().isEmpty()) {
                    member.setValue( convertFromXmlDatatypeToClient( memberAttribute.getAttributeValue().get( 0 ) ) );
                }
                compoundMembers.add( member );
            }
            attribute.setValue( new LinkIDCompound( compoundMembers ) );
        } else {
            // single/multi valued
            attribute.setValue( convertFromXmlDatatypeToClient( attributeValues.get( 0 ) ) );
        }
        return attribute;
    }

    private static String findAttributeId(AttributeType attribute) {

        return attribute.getOtherAttributes().get( LinkIDWebServiceConstants.ATTRIBUTE_ID );
    }

    @Override
    public Map<String, List<LinkIDAttribute<Serializable>>> getAttributes(String userId)
            throws LinkIDRequestDeniedException, LinkIDWSClientTransportException, LinkIDAttributeNotFoundException, LinkIDAttributeUnavailableException,
                   LinkIDSubjectNotFoundException {

        Map<String, List<LinkIDAttribute<Serializable>>> attributes = new HashMap<String, List<LinkIDAttribute<Serializable>>>();
        AttributeQueryType request = getAttributeQuery( userId, attributes );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
        return attributes;
    }

    @Override
    public List<LinkIDAttribute<Serializable>> getAttributes(String userId, String attributeName)
            throws LinkIDRequestDeniedException, LinkIDWSClientTransportException, LinkIDAttributeNotFoundException, LinkIDAttributeUnavailableException,
                   LinkIDSubjectNotFoundException {

        Map<String, List<LinkIDAttribute<Serializable>>> attributes = new HashMap<String, List<LinkIDAttribute<Serializable>>>();
        AttributeQueryType request = getAttributeQuery( userId, attributeName );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
        if (attributes.size() != 1) {
            throw new InternalInconsistencyException( "Requested 1 specified attribute but received multiple ?!" );
        }
        return attributes.get( attributeName );
    }

    @Nullable
    private static Serializable convertFromXmlDatatypeToClient(Object value) {

        if (null == value) {
            return null;
        }
        Object result = value;
        if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar calendar = (XMLGregorianCalendar) value;
            result = calendar.toGregorianCalendar().getTime();
        }
        return (Serializable) result;
    }
}
