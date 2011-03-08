/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import com.sun.xml.ws.client.ClientTransportException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import liberty.dst._2006_08.ref.safe_online.*;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.attribute.provider.AttributeAbstract;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.attribute.provider.Compound;
import net.link.safeonline.data.ws.DataServiceConstants;
import net.link.safeonline.data.ws.DataServiceFactory;
import net.link.safeonline.data.ws.SecondLevelStatusCode;
import net.link.safeonline.data.ws.TopLevelStatusCode;
import net.link.safeonline.sdk.logging.exception.RequestDeniedException;
import net.link.safeonline.sdk.logging.exception.WSClientTransportException;
import net.link.safeonline.sdk.ws.AbstractWSClient;
import net.link.safeonline.sdk.ws.WebServiceConstants;
import net.link.util.ws.pkix.wssecurity.WSSecurityClientHandler;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the data client. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class DataClientImpl extends AbstractWSClient implements DataClient {

    private static final Log LOG = LogFactory.getLog( DataClientImpl.class );

    private final DataServicePort port;

    private final String location;

    private final TargetIdentityClientHandler targetIdentityHandler;

    /**
     * Main constructor.
     *
     * @param location          the location (host:port) of the attribute web service.
     * @param clientCertificate the X509 certificate to use for WS-Security signature.
     * @param clientPrivateKey  the private key corresponding with the client certificate.
     * @param serverCertificate the X509 certificate of the server
     * @param maxOffset         the maximum offset of the WS-Security timestamp received. If <code>null</code> default offset configured in
     *                          {@link WSSecurityClientHandler} will be used.
     * @param sslCertificate    If not <code>null</code> will verify the server SSL {@link X509Certificate}.
     */
    public DataClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey,
                          X509Certificate serverCertificate, Long maxOffset, X509Certificate sslCertificate) {

        DataService dataService = DataServiceFactory.newInstance();
        AddressingFeature addressingFeature = new AddressingFeature();
        port = dataService.getDataServicePort( addressingFeature );
        this.location = location + "/data";

        setEndpointAddress();

        registerMessageLoggerHandler( port );

        /*
         * The order of the JAX-WS handlers is important. For outbound messages the TargetIdentity SOAP handler needs to come first since it
         * feeds additional XML Id's to be signed by the WS-Security handler.
         */
        targetIdentityHandler = new TargetIdentityClientHandler();
        initTargetIdentityHandler();

        registerTrustManager( port, sslCertificate );

        WSSecurityClientHandler.addNewHandler( port, clientCertificate, clientPrivateKey, serverCertificate, maxOffset );
    }

    /**
     * {@inheritDoc}
     */
    public void setAttributeValue(String userId, AttributeSDK<?> attribute)
            throws WSClientTransportException, RequestDeniedException {

        setAttributeValue( userId, Collections.<AttributeSDK<?>>singletonList( attribute ) );
    }

    /**
     * {@inheritDoc}
     */
    public void setAttributeValue(String userId, List<AttributeSDK<?>> attributes)
            throws WSClientTransportException, RequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        ModifyType modify = new ModifyType();
        for (AttributeSDK<?> attribute : attributes) {
            modify.getModifyItem().add( getModifyItem( attribute ) );
        }

        ModifyResponseType modifyResponse;
        try {
            modifyResponse = port.modify( modify );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }

        validateStatus( modifyResponse.getStatus() );
    }

    /**
     * {@inheritDoc}
     */
    public List<AttributeSDK<?>> getAttributes(String userId, String attributeName)
            throws WSClientTransportException, RequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        QueryType query = new QueryType();

        List<QueryItemType> queryItems = query.getQueryItem();
        QueryItemType queryItem = new QueryItemType();
        queryItems.add( queryItem );

        queryItem.setObjectType( DataServiceConstants.ATTRIBUTE_OBJECT_TYPE );
        SelectType select = new SelectType();
        select.setValue( attributeName );
        queryItem.setSelect( select );

        QueryResponseType queryResponse;
        try {
            queryResponse = port.query( query );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }

        validateStatus( queryResponse.getStatus() );

        // parse attributes
        List<AttributeSDK<?>> attributes = new LinkedList<AttributeSDK<?>>();

        List<DataType> dataList = queryResponse.getData();
        for (DataType data : dataList) {
            AttributeType attribute = data.getAttribute();
            attributes.add( getAttributeSDK( attribute ) );
        }

        return attributes;
    }

    /**
     * {@inheritDoc}
     */
    public void createAttribute(final String userId, final AttributeSDK<?> attribute)
            throws WSClientTransportException, RequestDeniedException {

        createAttributes( userId, Collections.<AttributeSDK<?>>singletonList( attribute ) );
    }

    /**
     * {@inheritDoc}
     */
    public void createAttributes(final String userId, final List<AttributeSDK<?>> attributes)
            throws WSClientTransportException, RequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        CreateType create = new CreateType();
        for (AttributeSDK<?> attribute : attributes) {
            create.getCreateItem().add( getCreateItem( attribute ) );
        }

        CreateResponseType createResponse;
        try {
            createResponse = port.create( create );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }

        validateStatus( createResponse.getStatus() );
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttributes(final String userId, final String attributeName)
            throws WSClientTransportException, RequestDeniedException {

        removeAttributes( userId, Collections.<AttributeSDK<?>>singletonList( new AttributeSDK<Serializable>( null, attributeName ) ) );
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttribute(final String userId, final String attributeName, final String attributeId)
            throws WSClientTransportException, RequestDeniedException {

        removeAttributes( userId,
                          Collections.<AttributeSDK<?>>singletonList( new AttributeSDK<Serializable>( attributeId, attributeName ) ) );
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttribute(final String userId, final AttributeSDK<?> attribute)
            throws WSClientTransportException, RequestDeniedException {

        removeAttributes( userId, Collections.<AttributeSDK<?>>singletonList( attribute ) );
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttributes(final String userId, final List<AttributeSDK<?>> attributes)
            throws WSClientTransportException, RequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        DeleteType delete = new DeleteType();
        for (AttributeSDK<?> attribute : attributes) {
            delete.getDeleteItem().add( getDeleteItem( attribute ) );
        }

        DeleteResponseType deleteResponse;
        try {
            deleteResponse = port.delete( delete );
        } catch (ClientTransportException e) {
            throw new WSClientTransportException( location, e );
        } catch (Exception e) {
            throw retrieveHeadersFromException( e );
        } finally {
            retrieveHeadersFromPort( port );
        }

        validateStatus( deleteResponse.getStatus() );
    }

    private void initTargetIdentityHandler() {

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add( targetIdentityHandler );
        binding.setHandlerChain( handlerChain );
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location );
    }

    private static AttributeType getAttributeType(AttributeSDK<?> attribute) {

        AttributeType attributeType = new AttributeType();
        attributeType.setName( attribute.getAttributeName() );
        if (null != attribute.getAttributeId())
            attributeType.getOtherAttributes().put( WebServiceConstants.ATTRIBUTE_ID, attribute.getAttributeId() );

        if (null != attribute.getValue()) {
            if (attribute.getValue() instanceof Compound) {

                // wrap members
                AttributeType compoundValueAttribute = new AttributeType();
                attributeType.getAttributeValue().add( compoundValueAttribute );

                // compounded
                Compound compound = (Compound) attribute.getValue();
                for (AttributeAbstract<?> memberAttribute : compound.getMembers()) {
                    AttributeSDK<?> member = (AttributeSDK<?>) memberAttribute;

                    AttributeType memberAttributeType = new AttributeType();
                    memberAttributeType.setName( member.getAttributeName() );
                    memberAttributeType.getAttributeValue().add( convertFromXmlDatatypeToClient( member.getValue() ) );

                    compoundValueAttribute.getAttributeValue().add( memberAttributeType );
                }
            } else {
                // single/multi valued
                attributeType.getAttributeValue().add( convertFromXmlDatatypeToClient( attribute.getValue() ) );
            }
        }
        return attributeType;
    }

    private static AttributeSDK<?> getAttributeSDK(AttributeType attributeType) {

        String attributeId = findAttributeId( attributeType );
        AttributeSDK<Serializable> attribute = new AttributeSDK<Serializable>( attributeId, attributeType.getName() );

        List<Object> attributeValues = attributeType.getAttributeValue();
        if (attributeValues.isEmpty())
            return attribute;

        if (attributeValues.get( 0 ) instanceof AttributeType) {

            // compound value
            AttributeType compoundValue = (AttributeType) attributeValues.get( 0 );

            // compound
            List<AttributeSDK<?>> compoundMembers = new LinkedList<AttributeSDK<?>>();
            for (Object memberAttributeObject : compoundValue.getAttributeValue()) {

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

    private static void validateStatus(StatusType status)
            throws RequestDeniedException {

        LOG.debug( "status: " + status.getCode() );
        TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode.fromCode( status.getCode() );

        if (TopLevelStatusCode.OK != topLevelStatusCode) {
            List<StatusType> secondLevelStatusList = status.getStatus();
            if (!secondLevelStatusList.isEmpty()) {
                StatusType secondLevelStatus = secondLevelStatusList.get( 0 );
                LOG.debug( "second level status: " + secondLevelStatus.getCode() );
                SecondLevelStatusCode secondLevelStatusCode = SecondLevelStatusCode.fromCode( secondLevelStatus.getCode() );
                LOG.debug( "second level status comment: " + secondLevelStatus.getComment() );
                switch (secondLevelStatusCode) {
                    case INVALID_DATA:
                        throw new RequestDeniedException( "Invalid data: " + secondLevelStatus.getComment() );
                    case DOES_NOT_EXIST:
                        throw new RequestDeniedException( "Does not exist: " + secondLevelStatus.getComment() );
                    case NOT_AUTHORIZED:
                        throw new RequestDeniedException( "Not authorized: " + secondLevelStatus.getComment() );
                    case UNSUPPORTED_OBJECT_TYPE:
                        throw new RequestDeniedException( "Object type unsupported: " + secondLevelStatus.getComment() );
                    case PAGINATION_NOT_SUPPORTED:
                        throw new RequestDeniedException( "Pagination not unsupported: " + secondLevelStatus.getComment() );
                    case MISSING_OBJECT_TYPE:
                        throw new RequestDeniedException( "Missing object type: " + secondLevelStatus.getComment() );
                    case EMPTY_REQUEST:
                        throw new RequestDeniedException( "Empty request: " + secondLevelStatus.getComment() );
                    case MISSING_SELECT:
                        throw new RequestDeniedException( "Missing select: " + secondLevelStatus.getComment() );
                    case MISSING_CREDENTIALS:
                        throw new RequestDeniedException( "Missing credentials: " + secondLevelStatus.getComment() );
                    case MISSING_NEW_DATA_ELEMENT:
                        throw new RequestDeniedException( "Missing NewData element: " + secondLevelStatus.getComment() );
                }
            }
            LOG.debug( "status comment: " + status.getComment() );
            throw new RequestDeniedException( "Request failed: errorCode=" + status.getCode() + " errorMessage=" + status.getComment() );
        }
    }

    private static ModifyItemType getModifyItem(AttributeSDK<?> attribute) {

        ModifyItemType modifyItem = new ModifyItemType();
        modifyItem.setObjectType( DataServiceConstants.ATTRIBUTE_OBJECT_TYPE );

        SelectType select = new SelectType();
        modifyItem.setSelect( select );
        select.setValue( attribute.getAttributeName() );

        AppDataType newData = new AppDataType();
        modifyItem.setNewData( newData );
        newData.setAttribute( getAttributeType( attribute ) );

        return modifyItem;
    }

    private static CreateItemType getCreateItem(AttributeSDK<?> attribute) {

        CreateItemType createItem = new CreateItemType();
        createItem.setObjectType( DataServiceConstants.ATTRIBUTE_OBJECT_TYPE );
        AppDataType newData = new AppDataType();
        newData.setAttribute( getAttributeType( attribute ) );
        createItem.setNewData( newData );
        return createItem;
    }

    private static DeleteItemType getDeleteItem(AttributeSDK<?> attribute) {

        DeleteItemType deleteItem = new DeleteItemType();
        deleteItem.setObjectType( DataServiceConstants.ATTRIBUTE_OBJECT_TYPE );
        SelectType select = new SelectType();
        select.setValue( attribute.getAttributeName() );
        if (null != attribute.getAttributeId())
            select.getOtherAttributes().put( WebServiceConstants.ATTRIBUTE_ID, attribute.getAttributeId() );

        return deleteItem;
    }
}
