/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import com.sun.xml.ws.client.ClientTransportException;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import liberty.dst._2006_08.ref.safe_online.AppDataType;
import liberty.dst._2006_08.ref.safe_online.CreateItemType;
import liberty.dst._2006_08.ref.safe_online.CreateResponseType;
import liberty.dst._2006_08.ref.safe_online.CreateType;
import liberty.dst._2006_08.ref.safe_online.DataServicePort;
import liberty.dst._2006_08.ref.safe_online.DataType;
import liberty.dst._2006_08.ref.safe_online.DeleteItemType;
import liberty.dst._2006_08.ref.safe_online.DeleteResponseType;
import liberty.dst._2006_08.ref.safe_online.DeleteType;
import liberty.dst._2006_08.ref.safe_online.ModifyItemType;
import liberty.dst._2006_08.ref.safe_online.ModifyResponseType;
import liberty.dst._2006_08.ref.safe_online.ModifyType;
import liberty.dst._2006_08.ref.safe_online.QueryItemType;
import liberty.dst._2006_08.ref.safe_online.QueryResponseType;
import liberty.dst._2006_08.ref.safe_online.QueryType;
import liberty.dst._2006_08.ref.safe_online.SelectType;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.attribute.LinkIDCompound;
import net.link.safeonline.sdk.api.exception.LinkIDRequestDeniedException;
import net.link.safeonline.sdk.api.exception.LinkIDWSClientTransportException;
import net.link.safeonline.sdk.api.ws.LinkIDWebServiceConstants;
import net.link.safeonline.sdk.api.ws.data.LinkIDDataServiceConstants;
import net.link.safeonline.sdk.api.ws.data.LinkIDSecondLevelStatusCode;
import net.link.safeonline.sdk.api.ws.data.LinkIDTopLevelStatusCode;
import net.link.safeonline.sdk.api.ws.data.client.LinkIDDataClient;
import net.link.safeonline.sdk.ws.LinkIDSDKUtils;
import net.link.safeonline.ws.data.LinkIDDataServiceFactory;
import net.link.util.logging.Logger;
import net.link.util.ws.AbstractWSClient;
import net.link.util.ws.security.username.WSSecurityUsernameTokenCallback;
import net.link.util.ws.security.username.WSSecurityUsernameTokenHandler;
import net.link.util.ws.security.x509.WSSecurityConfiguration;
import net.link.util.ws.security.x509.WSSecurityX509TokenHandler;
import oasis.names.tc.saml._2_0.assertion.AttributeType;


/**
 * Implementation of the data client. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 *
 * @author fcorneli
 */
public class LinkIDDataClientImpl extends AbstractWSClient<DataServicePort> implements LinkIDDataClient {

    private static final Logger logger = Logger.get( LinkIDDataClientImpl.class );

    private final LinkIDTargetIdentityClientHandler targetIdentityHandler;

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the attribute web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     * @param configuration   WS Security configuration
     */
    public LinkIDDataClientImpl(String location, X509Certificate[] sslCertificates, final WSSecurityConfiguration configuration) {

        this( location, sslCertificates );

        WSSecurityX509TokenHandler.install( getBindingProvider(), configuration );
    }

    /**
     * Main constructor.
     *
     * @param location        the location (host:port) of the ltqr web service.
     * @param sslCertificates If not {@code null} will verify the server SSL {@link X509Certificate}.
     */
    public LinkIDDataClientImpl(final String location, final X509Certificate[] sslCertificates, final WSSecurityUsernameTokenCallback usernameTokenCallback) {

        this( location, sslCertificates );

        WSSecurityUsernameTokenHandler.install( getBindingProvider(), usernameTokenCallback );
    }

    private LinkIDDataClientImpl(final String location, final X509Certificate[] sslCertificates) {

        super( LinkIDDataServiceFactory.newInstance().getDataServicePort( new AddressingFeature() ), sslCertificates );
        getBindingProvider().getRequestContext()
                            .put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                    String.format( "%s/%s", location, LinkIDSDKUtils.getSDKProperty( "linkid.ws.data.path" ) ) );

                /*
         * The order of the JAX-WS handlers is important. For outbound messages the TargetIdentity SOAP handler needs to come first since it
         * feeds additional XML Id's to be signed by the WS-Security handler.
         */
        targetIdentityHandler = new LinkIDTargetIdentityClientHandler();
        initTargetIdentityHandler();
    }

    @Override
    public void setAttributeValue(String userId, LinkIDAttribute<? extends Serializable> attribute)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        setAttributeValue( userId, Collections.<LinkIDAttribute<? extends Serializable>>singletonList( attribute ) );
    }

    @Override
    public void setAttributeValue(String userId, List<LinkIDAttribute<? extends Serializable>> attributes)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        ModifyType modify = new ModifyType();
        for (LinkIDAttribute<?> attribute : attributes) {
            modify.getModifyItem().add( getModifyItem( attribute ) );
        }

        ModifyResponseType modifyResponse;
        try {
            modifyResponse = getPort().modify( modify );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( modifyResponse.getStatus() );
    }

    @Override
    public <T extends Serializable> List<LinkIDAttribute<T>> getAttributes(String userId, String attributeName)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        QueryType query = new QueryType();

        List<QueryItemType> queryItems = query.getQueryItem();
        QueryItemType queryItem = new QueryItemType();
        queryItems.add( queryItem );

        queryItem.setObjectType( LinkIDDataServiceConstants.ATTRIBUTE_OBJECT_TYPE );
        SelectType select = new SelectType();
        select.setValue( attributeName );
        queryItem.setSelect( select );

        QueryResponseType queryResponse;
        try {
            queryResponse = getPort().query( query );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( queryResponse.getStatus() );

        // parse attributes
        List<LinkIDAttribute<T>> attributes = new LinkedList<LinkIDAttribute<T>>();

        List<DataType> dataList = queryResponse.getData();
        for (DataType data : dataList) {
            AttributeType attribute = data.getAttribute();
            //noinspection unchecked
            attributes.add( (LinkIDAttribute<T>) getAttributeSDK( attribute ) );
        }

        return attributes;
    }

    @Override
    public void createAttribute(final String userId, final LinkIDAttribute<? extends Serializable> attribute)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        createAttributes( userId, Collections.<LinkIDAttribute<? extends Serializable>>singletonList( attribute ) );
    }

    @Override
    public void createAttributes(final String userId, final List<LinkIDAttribute<? extends Serializable>> attributes)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        CreateType create = new CreateType();
        for (LinkIDAttribute<?> attribute : attributes) {
            create.getCreateItem().add( getCreateItem( attribute ) );
        }

        CreateResponseType createResponse;
        try {
            createResponse = getPort().create( create );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
        }

        validateStatus( createResponse.getStatus() );
    }

    @Override
    public void removeAttributes(final String userId, final String attributeName)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        removeAttributes( userId, Collections.<LinkIDAttribute<? extends Serializable>>singletonList( new LinkIDAttribute<Serializable>( attributeName, null ) ) );
    }

    @Override
    public void removeAttribute(final String userId, final String attributeName, final String attributeId)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        removeAttributes( userId,
                Collections.<LinkIDAttribute<? extends Serializable>>singletonList( new LinkIDAttribute<Serializable>( attributeId, attributeName, null ) ) );
    }

    @Override
    public void removeAttribute(final String userId, final LinkIDAttribute<? extends Serializable> attribute)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        removeAttributes( userId, Collections.<LinkIDAttribute<? extends Serializable>>singletonList( attribute ) );
    }

    @Override
    public void removeAttributes(final String userId, final List<LinkIDAttribute<? extends Serializable>> attributes)
            throws LinkIDWSClientTransportException, LinkIDRequestDeniedException {

        // set userId
        targetIdentityHandler.setTargetIdentity( userId );

        DeleteType delete = new DeleteType();
        for (LinkIDAttribute<?> attribute : attributes) {
            delete.getDeleteItem().add( getDeleteItem( attribute ) );
        }

        DeleteResponseType deleteResponse;
        try {
            deleteResponse = getPort().delete( delete );
        }
        catch (ClientTransportException e) {
            throw new LinkIDWSClientTransportException( getBindingProvider(), e );
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

    private static AttributeType getAttributeType(LinkIDAttribute<?> attribute) {

        AttributeType attributeType = new AttributeType();
        attributeType.setName( attribute.getName() );
        if (null != attribute.getId()) {
            attributeType.getOtherAttributes().put( LinkIDWebServiceConstants.ATTRIBUTE_ID, attribute.getId() );
        }

        if (null != attribute.getValue()) {
            if (attribute.getValue() instanceof LinkIDCompound) {

                // wrap members
                AttributeType compoundValueAttribute = new AttributeType();
                attributeType.getAttributeValue().add( compoundValueAttribute );

                // compounded
                LinkIDCompound linkIDCompound = (LinkIDCompound) attribute.getValue();
                for (LinkIDAttribute<?> memberAttribute : linkIDCompound.getMembers()) {

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

    private static LinkIDAttribute<? extends Serializable> getAttributeSDK(AttributeType attributeType) {

        String attributeId = findAttributeId( attributeType );
        LinkIDAttribute<Serializable> attribute = new LinkIDAttribute<Serializable>( attributeId, attributeType.getName(), null );

        List<Object> attributeValues = attributeType.getAttributeValue();
        if (attributeValues.isEmpty()) {
            return attribute;
        }

        if (attributeValues.get( 0 ) instanceof AttributeType) {

            // compound value
            AttributeType compoundValue = (AttributeType) attributeValues.get( 0 );

            // compound
            List<LinkIDAttribute<?>> compoundMembers = new LinkedList<LinkIDAttribute<?>>();
            for (Object memberAttributeObject : compoundValue.getAttributeValue()) {

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

    private static void validateStatus(StatusType status)
            throws LinkIDRequestDeniedException {

        logger.dbg( "status: " + status.getCode() );
        LinkIDTopLevelStatusCode linkIDTopLevelStatusCode = LinkIDTopLevelStatusCode.fromCode( status.getCode() );

        if (LinkIDTopLevelStatusCode.OK != linkIDTopLevelStatusCode) {
            List<StatusType> secondLevelStatusList = status.getStatus();
            if (!secondLevelStatusList.isEmpty()) {
                StatusType secondLevelStatus = secondLevelStatusList.get( 0 );
                logger.dbg( "second level status: " + secondLevelStatus.getCode() );
                LinkIDSecondLevelStatusCode linkIDSecondLevelStatusCode = LinkIDSecondLevelStatusCode.fromCode( secondLevelStatus.getCode() );
                logger.dbg( "second level status comment: " + secondLevelStatus.getComment() );
                switch (linkIDSecondLevelStatusCode) {
                    case INVALID_DATA:
                        throw new LinkIDRequestDeniedException( String.format( "Invalid data: %s", secondLevelStatus.getComment() ) );
                    case DOES_NOT_EXIST:
                        throw new LinkIDRequestDeniedException( String.format( "Does not exist: %s", secondLevelStatus.getComment() ) );
                    case NOT_AUTHORIZED:
                        throw new LinkIDRequestDeniedException( String.format( "Not authorized: %s", secondLevelStatus.getComment() ) );
                    case UNSUPPORTED_OBJECT_TYPE:
                        throw new LinkIDRequestDeniedException( String.format( "Object type unsupported: %s", secondLevelStatus.getComment() ) );
                    case PAGINATION_NOT_SUPPORTED:
                        throw new LinkIDRequestDeniedException( String.format( "Pagination not unsupported: %s", secondLevelStatus.getComment() ) );
                    case MISSING_OBJECT_TYPE:
                        throw new LinkIDRequestDeniedException( String.format( "Missing object type: %s", secondLevelStatus.getComment() ) );
                    case EMPTY_REQUEST:
                        throw new LinkIDRequestDeniedException( String.format( "Empty request: %s", secondLevelStatus.getComment() ) );
                    case MISSING_SELECT:
                        throw new LinkIDRequestDeniedException( String.format( "Missing select: %s", secondLevelStatus.getComment() ) );
                    case MISSING_CREDENTIALS:
                        throw new LinkIDRequestDeniedException( String.format( "Missing credentials: %s", secondLevelStatus.getComment() ) );
                    case MISSING_NEW_DATA_ELEMENT:
                        throw new LinkIDRequestDeniedException( String.format( "Missing NewData element: %s", secondLevelStatus.getComment() ) );
                }
            }
            logger.dbg( "status comment: " + status.getComment() );
            throw new LinkIDRequestDeniedException( String.format( "Request failed: errorCode=%s errorMessage=%s", status.getCode(), status.getComment() ) );
        }
    }

    private static ModifyItemType getModifyItem(LinkIDAttribute<?> attribute) {

        ModifyItemType modifyItem = new ModifyItemType();
        modifyItem.setObjectType( LinkIDDataServiceConstants.ATTRIBUTE_OBJECT_TYPE );

        SelectType select = new SelectType();
        modifyItem.setSelect( select );
        select.setValue( attribute.getName() );

        AppDataType newData = new AppDataType();
        modifyItem.setNewData( newData );
        newData.setAttribute( getAttributeType( attribute ) );

        return modifyItem;
    }

    private static CreateItemType getCreateItem(LinkIDAttribute<?> attribute) {

        CreateItemType createItem = new CreateItemType();
        createItem.setObjectType( LinkIDDataServiceConstants.ATTRIBUTE_OBJECT_TYPE );
        AppDataType newData = new AppDataType();
        newData.setAttribute( getAttributeType( attribute ) );
        createItem.setNewData( newData );
        return createItem;
    }

    private static DeleteItemType getDeleteItem(LinkIDAttribute<?> attribute) {

        DeleteItemType deleteItem = new DeleteItemType();
        deleteItem.setObjectType( LinkIDDataServiceConstants.ATTRIBUTE_OBJECT_TYPE );
        SelectType select = new SelectType();
        select.setValue( attribute.getName() );
        if (null != attribute.getId()) {
            select.getOtherAttributes().put( LinkIDWebServiceConstants.ATTRIBUTE_ID, attribute.getId() );
        }

        deleteItem.setSelect( select );
        return deleteItem;
    }
}
