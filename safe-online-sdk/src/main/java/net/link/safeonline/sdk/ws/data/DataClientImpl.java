/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import com.sun.xml.internal.ws.client.ClientTransportException;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import liberty.dst._2006_08.ref.safe_online.*;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.sdk.SDKUtils;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.attribute.Compound;
import net.link.safeonline.sdk.api.exception.RequestDeniedException;
import net.link.safeonline.sdk.api.exception.WSClientTransportException;
import net.link.safeonline.sdk.api.ws.WebServiceConstants;
import net.link.safeonline.sdk.api.ws.data.*;
import net.link.safeonline.sdk.api.ws.data.client.DataClient;
import net.link.safeonline.ws.data.DataServiceFactory;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.WSSecurityConfiguration;
import net.link.util.ws.security.WSSecurityHandler;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the data client. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class DataClientImpl extends AbstractWSClient<DataServicePort> implements DataClient {

    private static final Log LOG = LogFactory.getLog( DataClientImpl.class );

    private final TargetIdentityClientHandler targetIdentityHandler;

    /**
     * Main constructor.
     *
     * @param location       the location (host:port) of the attribute web service.
     * @param sslCertificate If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration  The WS-Security configuration.
     */
    public DataClientImpl(String location, X509Certificate sslCertificate, final WSSecurityConfiguration configuration) {

        super( DataServiceFactory.newInstance().getDataServicePort( new AddressingFeature() ), sslCertificate );
        getBindingProvider().getRequestContext()
                .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                        String.format( "%s/%s", location, SDKUtils.getSDKProperty( "linkid.ws.data.path" ) ) );

        /*
         * The order of the JAX-WS handlers is important. For outbound messages the TargetIdentity SOAP handler needs to come first since it
         * feeds additional XML Id's to be signed by the WS-Security handler.
         */
        targetIdentityHandler = new TargetIdentityClientHandler();
        initTargetIdentityHandler();

        WSSecurityHandler.install( getBindingProvider(), configuration );
    }

    @Override
    public void setAttributeValue(String userId, AttributeSDK<? extends Serializable> attribute)
            throws WSClientTransportException, RequestDeniedException {

        setAttributeValue( userId, Collections.<AttributeSDK<? extends Serializable>>singletonList( attribute ) );
    }

    @Override
    public void setAttributeValue(String userId, List<AttributeSDK<? extends Serializable>> attributes)
            throws WSClientTransportException, RequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        ModifyType modify = new ModifyType();
        for (AttributeSDK<?> attribute : attributes) {
            modify.getModifyItem().add( getModifyItem( attribute ) );
        }

        ModifyResponseType modifyResponse;
        try {
            modifyResponse = getPort().modify( modify );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( modifyResponse.getStatus() );
    }

    @Override
    public <T extends Serializable> List<AttributeSDK<T>> getAttributes(String userId, String attributeName)
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
            queryResponse = getPort().query( query );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( queryResponse.getStatus() );

        // parse attributes
        List<AttributeSDK<T>> attributes = new LinkedList<AttributeSDK<T>>();

        List<DataType> dataList = queryResponse.getData();
        for (DataType data : dataList) {
            AttributeType attribute = data.getAttribute();
            attributes.add( (AttributeSDK<T>) getAttributeSDK( attribute ) );
        }

        return attributes;
    }

    @Override
    public void createAttribute(final String userId, final AttributeSDK<? extends Serializable> attribute)
            throws WSClientTransportException, RequestDeniedException {

        createAttributes( userId, Collections.<AttributeSDK<? extends Serializable>>singletonList( attribute ) );
    }

    @Override
    public void createAttributes(final String userId, final List<AttributeSDK<? extends Serializable>> attributes)
            throws WSClientTransportException, RequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        CreateType create = new CreateType();
        for (AttributeSDK<?> attribute : attributes) {
            create.getCreateItem().add( getCreateItem( attribute ) );
        }

        CreateResponseType createResponse;
        try {
            createResponse = getPort().create( create );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( createResponse.getStatus() );
    }

    @Override
    public void removeAttributes(final String userId, final String attributeName)
            throws WSClientTransportException, RequestDeniedException {

        removeAttributes( userId,
                Collections.<AttributeSDK<? extends Serializable>>singletonList( new AttributeSDK<Serializable>( attributeName, null ) ) );
    }

    @Override
    public void removeAttribute(final String userId, final String attributeName, final String attributeId)
            throws WSClientTransportException, RequestDeniedException {

        removeAttributes( userId, Collections.<AttributeSDK<? extends Serializable>>singletonList(
                new AttributeSDK<Serializable>( attributeId, attributeName, null ) ) );
    }

    @Override
    public void removeAttribute(final String userId, final AttributeSDK<? extends Serializable> attribute)
            throws WSClientTransportException, RequestDeniedException {

        removeAttributes( userId, Collections.<AttributeSDK<? extends Serializable>>singletonList( attribute ) );
    }

    @Override
    public void removeAttributes(final String userId, final List<AttributeSDK<? extends Serializable>> attributes)
            throws WSClientTransportException, RequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        DeleteType delete = new DeleteType();
        for (AttributeSDK<?> attribute : attributes) {
            delete.getDeleteItem().add( getDeleteItem( attribute ) );
        }

        DeleteResponseType deleteResponse;
        try {
            deleteResponse = getPort().delete( delete );
        }
        catch (ClientTransportException e) {
            throw new WSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( deleteResponse.getStatus() );
    }

    private void initTargetIdentityHandler() {

        Binding binding = getBindingProvider().getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add( targetIdentityHandler );
        binding.setHandlerChain( handlerChain );
    }

    private static AttributeType getAttributeType(AttributeSDK<?> attribute) {

        AttributeType attributeType = new AttributeType();
        attributeType.setName( attribute.getName() );
        if (null != attribute.getId())
            attributeType.getOtherAttributes().put( WebServiceConstants.ATTRIBUTE_ID, attribute.getId() );

        if (null != attribute.getValue()) {
            if (attribute.getValue() instanceof Compound) {

                // wrap members
                AttributeType compoundValueAttribute = new AttributeType();
                attributeType.getAttributeValue().add( compoundValueAttribute );

                // compounded
                Compound compound = (Compound) attribute.getValue();
                for (AttributeSDK<?> memberAttribute : compound.getMembers()) {

                    AttributeType memberAttributeType = new AttributeType();
                    memberAttributeType.setName( memberAttribute.getName() );
                    memberAttributeType.getAttributeValue().add( convertFromXmlDatatypeToClient( memberAttribute.getValue() ) );

                    compoundValueAttribute.getAttributeValue().add( memberAttributeType );
                }
            } else {
                // single/multi valued
                attributeType.getAttributeValue().add( convertFromXmlDatatypeToClient( attribute.getValue() ) );
            }
        }
        return attributeType;
    }

    private static AttributeSDK<? extends Serializable> getAttributeSDK(AttributeType attributeType) {

        String attributeId = findAttributeId( attributeType );
        AttributeSDK<Serializable> attribute = new AttributeSDK<Serializable>( attributeId, attributeType.getName(), null );

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
                        throw new RequestDeniedException( String.format( "Invalid data: %s", secondLevelStatus.getComment() ) );
                    case DOES_NOT_EXIST:
                        throw new RequestDeniedException( String.format( "Does not exist: %s", secondLevelStatus.getComment() ) );
                    case NOT_AUTHORIZED:
                        throw new RequestDeniedException( String.format( "Not authorized: %s", secondLevelStatus.getComment() ) );
                    case UNSUPPORTED_OBJECT_TYPE:
                        throw new RequestDeniedException( String.format( "Object type unsupported: %s", secondLevelStatus.getComment() ) );
                    case PAGINATION_NOT_SUPPORTED:
                        throw new RequestDeniedException(
                                String.format( "Pagination not unsupported: %s", secondLevelStatus.getComment() ) );
                    case MISSING_OBJECT_TYPE:
                        throw new RequestDeniedException( String.format( "Missing object type: %s", secondLevelStatus.getComment() ) );
                    case EMPTY_REQUEST:
                        throw new RequestDeniedException( String.format( "Empty request: %s", secondLevelStatus.getComment() ) );
                    case MISSING_SELECT:
                        throw new RequestDeniedException( String.format( "Missing select: %s", secondLevelStatus.getComment() ) );
                    case MISSING_CREDENTIALS:
                        throw new RequestDeniedException( String.format( "Missing credentials: %s", secondLevelStatus.getComment() ) );
                    case MISSING_NEW_DATA_ELEMENT:
                        throw new RequestDeniedException( String.format( "Missing NewData element: %s", secondLevelStatus.getComment() ) );
                }
            }
            LOG.debug( "status comment: " + status.getComment() );
            throw new RequestDeniedException(
                    String.format( "Request failed: errorCode=%s errorMessage=%s", status.getCode(), status.getComment() ) );
        }
    }

    private static ModifyItemType getModifyItem(AttributeSDK<?> attribute) {

        ModifyItemType modifyItem = new ModifyItemType();
        modifyItem.setObjectType( DataServiceConstants.ATTRIBUTE_OBJECT_TYPE );

        SelectType select = new SelectType();
        modifyItem.setSelect( select );
        select.setValue( attribute.getName() );

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
        select.setValue( attribute.getName() );
        if (null != attribute.getId())
            select.getOtherAttributes().put( WebServiceConstants.ATTRIBUTE_ID, attribute.getId() );

        deleteItem.setSelect( select );
        return deleteItem;
    }
}
