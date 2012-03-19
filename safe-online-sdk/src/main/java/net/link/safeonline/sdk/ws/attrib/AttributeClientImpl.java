/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.attrib;

import com.lyndir.lhunath.opal.system.logging.Logger;
import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
import com.sun.xml.ws.client.ClientTransportException;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import net.link.safeonline.sdk.SDKUtils;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.attribute.Compound;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.*;
import net.link.safeonline.sdk.api.ws.attrib.client.AttributeClient;
import net.link.safeonline.ws.attrib.SAMLAttributeServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import oasis.names.tc.saml._2_0.assertion.*;
import oasis.names.tc.saml._2_0.assertion.ObjectFactory;
import oasis.names.tc.saml._2_0.protocol.*;
import org.jetbrains.annotations.Nullable;


/**
 * Implementation of attribute client. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class AttributeClientImpl extends AbstractWSClient<SAMLAttributePort> implements AttributeClient {

    static final Logger logger = Logger.get( AttributeClientImpl.class );

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  WS Security configuration
     */
    public AttributeClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( SAMLAttributeServiceFactory.newInstance().getSAMLAttributePort() );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.attribute.path" ) ) );

        registerTrustManager( sslCertificate );
        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

    private ResponseType getResponse(AttributeQueryType request)
            throws WSClientTransportException {

        try {
            return getPort().attributeQuery( request );
        }
        catch (ClientTransportException e) {
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

                if (SamlpSecondLevelErrorCode.REQUEST_DENIED == samlpSecondLevelErrorCode)
                    throw new RequestDeniedException();

                if (SamlpSecondLevelErrorCode.ATTRIBUTE_UNAVAILABLE == samlpSecondLevelErrorCode)
                    throw new AttributeUnavailableException();

                if (SamlpSecondLevelErrorCode.UNKNOWN_PRINCIPAL == samlpSecondLevelErrorCode)
                    throw new SubjectNotFoundException();

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

    private static AttributeQueryType getAttributeQuery(String userId, Map<String, List<AttributeSDK<Serializable>>> attributes) {

        Set<String> attributeNames = attributes.keySet();
        return getAttributeQuery( userId, attributeNames );
    }

    @Override
    public void getAttributes(String userId, Map<String, List<AttributeSDK<Serializable>>> attributes)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException, AttributeUnavailableException,
                   SubjectNotFoundException {

        AttributeQueryType request = getAttributeQuery( userId, attributes );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
    }

    private static void getAttributeValues(ResponseType response, Map<String, List<AttributeSDK<Serializable>>> attributeMap) {

        List<Object> assertions = response.getAssertionOrEncryptedAssertion();
        if (assertions.isEmpty())
            throw new InternalInconsistencyException( "No assertions in response" );
        AssertionType assertion = (AssertionType) assertions.get( 0 );

        List<StatementAbstractType> statements = assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement();
        if (statements.isEmpty())
            throw new InternalInconsistencyException( "No statements in response assertion" );
        AttributeStatementType attributeStatement = (AttributeStatementType) statements.get( 0 );

        for (Object attributeTypeObject : attributeStatement.getAttributeOrEncryptedAttribute()) {
            AttributeType attributeType = (AttributeType) attributeTypeObject;

            AttributeSDK<Serializable> attribute = getAttributeSDK( attributeType );

            List<AttributeSDK<Serializable>> attributes = attributeMap.get( attribute.getName() );
            if (null == attributes) {
                attributes = new LinkedList<AttributeSDK<Serializable>>();
            }
            attributes.add( attribute );
            attributeMap.put( attribute.getName(), attributes );
        }
    }

    private static AttributeSDK<Serializable> getAttributeSDK(AttributeType attributeType) {

        String attributeId = findAttributeId( attributeType );
        AttributeSDK<Serializable> attribute = new AttributeSDK<Serializable>( attributeId, attributeType.getName(), null );

        List<Object> attributeValues = attributeType.getAttributeValue();
        if (attributeValues.isEmpty())
            return attribute;

        if (attributeType.getAttributeValue().get( 0 ) instanceof AttributeType) {

            AttributeType compoundValueAttribute = (AttributeType) attributeType.getAttributeValue().get( 0 );

            // compound
            List<AttributeSDK<?>> compoundMembers = new LinkedList<AttributeSDK<?>>();
            for (Object memberAttributeObject : compoundValueAttribute.getAttributeValue()) {

                AttributeType memberAttribute = (AttributeType) memberAttributeObject;
                AttributeSDK<Serializable> member = new AttributeSDK<Serializable>( attributeId, memberAttribute.getName(), null );
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

    @Override
    public Map<String, List<AttributeSDK<Serializable>>> getAttributes(String userId)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException {

        Map<String, List<AttributeSDK<Serializable>>> attributes = new HashMap<String, List<AttributeSDK<Serializable>>>();
        AttributeQueryType request = getAttributeQuery( userId, attributes );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
        return attributes;
    }

    @Override
    public List<AttributeSDK<Serializable>> getAttributes(String userId, String attributeName)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException {

        Map<String, List<AttributeSDK<Serializable>>> attributes = new HashMap<String, List<AttributeSDK<Serializable>>>();
        AttributeQueryType request = getAttributeQuery( userId, attributeName );
        ResponseType response = getResponse( request );
        validateStatus( response );
        getAttributeValues( response, attributes );
        if (attributes.size() != 1)
            throw new InternalInconsistencyException( "Requested 1 specified attribute but received multiple ?!" );
        return attributes.get( attributeName );
    }

    @Nullable
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
