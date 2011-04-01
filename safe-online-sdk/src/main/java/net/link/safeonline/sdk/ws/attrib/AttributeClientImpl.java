/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.attrib;

import com.sun.xml.ws.client.ClientTransportException;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import net.link.safeonline.attrib.ws.SAMLAttributeServiceFactory;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.attribute.provider.Compound;
import net.link.safeonline.sdk.logging.exception.*;
import net.link.safeonline.sdk.ws.SamlpSecondLevelErrorCode;
import net.link.safeonline.sdk.ws.SamlpTopLevelErrorCode;
import net.link.safeonline.sdk.ws.WebServiceConstants;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import oasis.names.tc.saml._2_0.assertion.*;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.protocol.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of attribute client. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class AttributeClientImpl extends AbstractWSClient<SAMLAttributePort> implements AttributeClient {

    static final Log LOG = LogFactory.getLog( AttributeClientImpl.class );

    private final String location;

    /**
     * Main constructor.
     *
     * @param location          the location (host:port) of the attribute web service.
     * @param sslCertificate    If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     * @param configuration
     */
    public AttributeClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( SAMLAttributeServiceFactory.newInstance().getSAMLAttributePort() );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, this.location = MessageFormat.format( "{0}/attrib", location ) );

        registerTrustManager( sslCertificate );
        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

    private ResponseType getResponse(AttributeQueryType request)
            throws WSClientTransportException {

        try {
            return getPort().attributeQuery( request );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }
    }

    private static void validateStatus(ResponseType response)
            throws AttributeNotFoundException, RequestDeniedException, AttributeUnavailableException, SubjectNotFoundException {

        StatusType status = response.getStatus();
        StatusCodeType statusCode = status.getStatusCode();
        String statusCodeValue = statusCode.getValue();
        SamlpTopLevelErrorCode samlpTopLevelErrorCode = SamlpTopLevelErrorCode.getSamlpTopLevelErrorCode( statusCodeValue );
        if (SamlpTopLevelErrorCode.SUCCESS != samlpTopLevelErrorCode) {
            StatusCodeType secondLevelStatusCode = statusCode.getStatusCode();
            if (null != secondLevelStatusCode) {
                String secondLevelStatusCodeValue = secondLevelStatusCode.getValue();
                SamlpSecondLevelErrorCode samlpSecondLevelErrorCode = SamlpSecondLevelErrorCode.getSamlpTopLevelErrorCode(
                        secondLevelStatusCodeValue );
                if (SamlpSecondLevelErrorCode.INVALID_ATTRIBUTE_NAME_OR_VALUE == samlpSecondLevelErrorCode)
                    throw new AttributeNotFoundException();
                else if (SamlpSecondLevelErrorCode.REQUEST_DENIED == samlpSecondLevelErrorCode)
                    throw new RequestDeniedException();
                else if (SamlpSecondLevelErrorCode.ATTRIBUTE_UNAVAILABLE == samlpSecondLevelErrorCode)
                    throw new AttributeUnavailableException();
                else if (SamlpSecondLevelErrorCode.UNKNOWN_PRINCIPAL == samlpSecondLevelErrorCode)
                    throw new SubjectNotFoundException();
                LOG.debug( "second level status code: " + secondLevelStatusCode.getValue() );
            }
            throw new RuntimeException( "error: " + statusCodeValue );
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

    private static AttributeQueryType getAttributeQuery(String userId, Map<String, List<AttributeSDK<?>>> attributes) {

        Set<String> attributeNames = attributes.keySet();
        return getAttributeQuery( userId, attributeNames );
    }

    /**
     * {@inheritDoc}
     */
    public void getAttributes(String userId, Map<String, List<AttributeSDK<?>>> attributes)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException, AttributeUnavailableException,
                   SubjectNotFoundException {

        AttributeQueryType request = getAttributeQuery( userId, attributes );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
    }

    private static void getAttributeValues(ResponseType response, Map<String, List<AttributeSDK<?>>> attributeMap) {

        List<Object> assertions = response.getAssertionOrEncryptedAssertion();
        if (assertions.isEmpty())
            throw new RuntimeException( "No assertions in response" );
        AssertionType assertion = (AssertionType) assertions.get( 0 );

        List<StatementAbstractType> statements = assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement();
        if (statements.isEmpty())
            throw new RuntimeException( "No statements in response assertion" );
        AttributeStatementType attributeStatement = (AttributeStatementType) statements.get( 0 );

        for (Object attributeTypeObject : attributeStatement.getAttributeOrEncryptedAttribute()) {
            AttributeType attributeType = (AttributeType) attributeTypeObject;

            AttributeSDK<?> attribute = getAttributeSDK( attributeType );

            List<AttributeSDK<?>> attributes = attributeMap.get( attribute.getName() );
            if (null == attributes) {
                attributes = new LinkedList<AttributeSDK<?>>();
            }
            attributes.add( attribute );
            attributeMap.put( attribute.getName(), attributes );
        }
    }

    private static AttributeSDK<?> getAttributeSDK(AttributeType attributeType) {

        String attributeId = findAttributeId( attributeType );
        AttributeSDK<Serializable> attribute = new AttributeSDK<Serializable>( attributeId, attributeType.getName() );

        List<Object> attributeValues = attributeType.getAttributeValue();
        if (attributeValues.isEmpty())
            return attribute;

        if (attributeType.getAttributeValue().get( 0 ) instanceof AttributeType) {

            AttributeType compoundValueAttribute = (AttributeType) attributeType.getAttributeValue().get( 0 );

            // compound
            List<AttributeSDK<?>> compoundMembers = new LinkedList<AttributeSDK<?>>();
            for (Object memberAttributeObject : compoundValueAttribute.getAttributeValue()) {

                AttributeType memberAttribute = (AttributeType) memberAttributeObject;
                AttributeSDK<Serializable> member = new AttributeSDK<Serializable>( attributeId, memberAttribute.getName() );
                if (!memberAttribute.getAttributeValue().isEmpty()) {
                    member.setValue( convertFromXmlDatatypeToClient( memberAttribute.getAttributeValue().get( 0 ) ) );
                }
                compoundMembers.add( member );
            }
            attribute.setValue( new Compound( compoundMembers ) );
        } else {
            // single/multi valued
            attribute.setValue( convertFromXmlDatatypeToClient( attributeValues.get( 0 ) ) );
        }
        return attribute;
    }

    private static String findAttributeId(AttributeType attribute) {

        return attribute.getOtherAttributes().get( WebServiceConstants.ATTRIBUTE_ID );
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<AttributeSDK<?>>> getAttributes(String userId)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException {

        Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
        AttributeQueryType request = getAttributeQuery( userId, attributes );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
        return attributes;
    }

    /**
     * {@inheritDoc}
     */
    public List<AttributeSDK<?>> getAttributes(String userId, String attributeName)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException {

        Map<String, List<AttributeSDK<?>>> attributes = new HashMap<String, List<AttributeSDK<?>>>();
        AttributeQueryType request = getAttributeQuery( userId, attributeName );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
        if (attributes.size() != 1)
            throw new RuntimeException( "Requested 1 specified attribute but received multiple ?!" );
        return attributes.get( attributeName );
    }

    private static Serializable convertFromXmlDatatypeToClient(Object value) {

        if (null == value)
            return null;
        Object result = value;
        if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar calendar = (XMLGregorianCalendar) value;
            result = calendar.toGregorianCalendar().getTime();
        }
        return (Serializable) result;
    }
}
