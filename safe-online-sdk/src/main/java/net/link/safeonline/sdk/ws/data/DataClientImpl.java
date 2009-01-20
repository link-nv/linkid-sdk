/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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
import liberty.dst._2006_08.ref.safe_online.DataService;
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
import net.link.safeonline.data.ws.DataServiceConstants;
import net.link.safeonline.data.ws.DataServiceFactory;
import net.link.safeonline.data.ws.SecondLevelStatusCode;
import net.link.safeonline.data.ws.TopLevelStatusCode;
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.CompoundBuilder;
import net.link.safeonline.sdk.ws.CompoundUtil;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundId;
import net.link.safeonline.sdk.ws.annotation.CompoundMember;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.ws.common.WebServiceConstants;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;


/**
 * Implementation of the data client. This class is using JAX-WS, secured via WS-Security and server-side SSL.
 * 
 * @author fcorneli
 * 
 */
public class DataClientImpl extends AbstractMessageAccessor implements DataClient {

    private static final Log                  LOG = LogFactory.getLog(DataClientImpl.class);

    private final DataServicePort             port;

    private final String                      location;

    private final TargetIdentityClientHandler targetIdentityHandler;


    /**
     * Main constructor.
     * 
     * @param location
     *            the location (i.e. host:port) of the data web service.
     * @param clientCertificate
     * @param clientPrivateKey
     */
    public DataClientImpl(String location, X509Certificate clientCertificate, PrivateKey clientPrivateKey) {

        DataService dataService = DataServiceFactory.newInstance();
        AddressingFeature addressingFeature = new AddressingFeature();
        port = dataService.getDataServicePort(addressingFeature);
        this.location = location + "/safe-online-ws/data";

        setEndpointAddress();

        registerMessageLoggerHandler(port);

        /*
         * The order of the JAX-WS handlers is important. For outbound messages the TargetIdentity SOAP handler needs to come first since it
         * feeds additional XML Id's to be signed by the WS-Security handler.
         */
        targetIdentityHandler = new TargetIdentityClientHandler();
        initTargetIdentityHandler();

        WSSecurityClientHandler.addNewHandler(port, clientCertificate, clientPrivateKey);
    }

    private void initTargetIdentityHandler() {

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add(targetIdentityHandler);
        binding.setHandlerChain(handlerChain);
    }

    private void setEndpointAddress() {

        BindingProvider bindingProvider = (BindingProvider) port;

        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
    }

    public void setAttributeValue(String subjectLogin, String attributeName, Object attributeValue)
            throws WSClientTransportException, AttributeNotFoundException {

        targetIdentityHandler.setTargetIdentity(subjectLogin);

        ModifyType modify = new ModifyType();
        List<ModifyItemType> modifyItems = modify.getModifyItem();
        ModifyItemType modifyItem = new ModifyItemType();
        modifyItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        modifyItems.add(modifyItem);

        SelectType select = new SelectType();
        modifyItem.setSelect(select);
        select.setValue(attributeName);

        AppDataType newData = new AppDataType();
        modifyItem.setNewData(newData);
        AttributeType attribute = new AttributeType();
        attribute.setName(attributeName);
        setAttributeValue(attributeValue, attribute, false);
        newData.setAttribute(attribute);

        ModifyResponseType modifyResponse;
        try {
            modifyResponse = port.modify(modify);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(location);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(port);
        }

        StatusType status = modifyResponse.getStatus();
        LOG.debug("status: " + modifyResponse.getStatus().getCode());
        TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode.fromCode(status.getCode());

        if (TopLevelStatusCode.OK != topLevelStatusCode) {
            List<StatusType> secondLevelStatusList = status.getStatus();
            if (secondLevelStatusList.size() > 0) {
                StatusType secondLevelStatus = secondLevelStatusList.get(0);
                LOG.debug("second level status: " + secondLevelStatus.getCode());
                SecondLevelStatusCode secondLevelStatusCode = SecondLevelStatusCode.fromCode(secondLevelStatus.getCode());
                LOG.debug("second level status comment: " + secondLevelStatus.getComment());
                switch (secondLevelStatusCode) {
                    case INVALID_DATA:
                        throw new IllegalArgumentException("attribute value type incorrect");
                    case DOES_NOT_EXIST:
                        throw new AttributeNotFoundException();
                    default:
                }
            }
            LOG.debug("status comment: " + status.getComment());
            throw new RuntimeException("could not set the attribute");
        }
    }

    @SuppressWarnings("unchecked")
    public <Type> Attribute<Type> getAttributeValue(String userId, String attributeName, Class<Type> expectedValueClass)
            throws WSClientTransportException, RequestDeniedException, SubjectNotFoundException {

        targetIdentityHandler.setTargetIdentity(userId);

        QueryType query = new QueryType();

        List<QueryItemType> queryItems = query.getQueryItem();
        QueryItemType queryItem = new QueryItemType();
        queryItems.add(queryItem);

        queryItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        queryItem.setSelect(select);

        SafeOnlineTrustManager.configureSsl();

        QueryResponseType queryResponse;
        try {
            queryResponse = port.query(query);
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(location);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(port);
        }

        StatusType status = queryResponse.getStatus();
        LOG.debug("status: " + status.getCode());
        TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode.fromCode(status.getCode());
        switch (topLevelStatusCode) {
            case FAILED:
                List<StatusType> secondLevelStatuses = status.getStatus();
                if (0 == secondLevelStatuses.size())
                    throw new RuntimeException("ID-WSF DST error");
                StatusType secondLevelStatus = secondLevelStatuses.get(0);
                SecondLevelStatusCode secondLevelStatusCode = SecondLevelStatusCode.fromCode(secondLevelStatus.getCode());
                if (SecondLevelStatusCode.NOT_AUTHORIZED == secondLevelStatusCode)
                    throw new RequestDeniedException();
                if (SecondLevelStatusCode.DOES_NOT_EXIST == secondLevelStatusCode)
                    throw new SubjectNotFoundException();
                throw new RuntimeException("unknown error occurred");
            case OK:
            break;
            default:
                throw new RuntimeException("Unknown top level status code: " + topLevelStatusCode);
        }

        List<DataType> dataList = queryResponse.getData();
        if (0 == dataList.size()) {
            LOG.debug("no data entry");
            return null;
        }
        DataType data = dataList.get(0);
        AttributeType attribute = data.getAttribute();
        if (null == attribute)
            /*
             * This happens when the attribute entity does not exist.
             */
            return null;
        String name = attribute.getName();
        List<Object> attributeValues = attribute.getAttributeValue();
        Object firstAttributeValue = convertFromXmlDatatypeToClientDatatype(attributeValues.get(0));
        if (null == firstAttributeValue) {
            /*
             * null does not have a type. Lucky us.
             */
            Attribute<Type> dataValue = new Attribute<Type>(name, null);
            return dataValue;
        }

        /*
         * We also perform some type-checking on the received attribute values.
         */
        if (expectedValueClass.isArray()) {
            /*
             * Multi-valued attribute expected.
             */
            if (false == Boolean.TRUE.toString().equals(attribute.getOtherAttributes().get(WebServiceConstants.MULTIVALUED_ATTRIBUTE))) {
                String msg = "expected multivalued attribute, but received single-valued attribute";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            Class componentType = expectedValueClass.getComponentType();
            int size = attributeValues.size();
            Type values = (Type) Array.newInstance(componentType, size);
            for (int idx = 0; idx < size; idx++) {
                Object attributeValue = convertFromXmlDatatypeToClientDatatype(attributeValues.get(idx));
                if (attributeValue instanceof AttributeType) {
                    /*
                     * We're dealing with a compounded attribute here.
                     */
                    AttributeType compoundAttribute = (AttributeType) attributeValue;
                    CompoundBuilder compoundBuilder = new CompoundBuilder(componentType);

                    String attributeId = compoundAttribute.getOtherAttributes().get(WebServiceConstants.COMPOUNDED_ATTRIBUTE_ID);
                    compoundBuilder.setCompoundId(attributeId);

                    List<Object> memberAttributes = compoundAttribute.getAttributeValue();
                    for (Object memberAttributeObject : memberAttributes) {
                        AttributeType memberAttribute = (AttributeType) memberAttributeObject;
                        String memberName = memberAttribute.getName();
                        Object memberAttributeValue = convertFromXmlDatatypeToClientDatatype(memberAttribute.getAttributeValue().get(0));
                        compoundBuilder.setCompoundProperty(memberName, memberAttributeValue);
                    }

                    Array.set(values, idx, compoundBuilder.getCompound());
                } else {
                    if (false == componentType.isInstance(attributeValue))
                        throw new IllegalArgumentException("expected type " + componentType.getName() + "; received: "
                                + attributeValue.getClass().getName());
                    Array.set(values, idx, attributeValue);
                }
            }
            Attribute<Type> resultAttribute = new Attribute<Type>(attributeName, values);
            return resultAttribute;
        }

        /*
         * Single-valued attribute expected.
         */
        if (false == expectedValueClass.isInstance(firstAttributeValue))
            throw new IllegalArgumentException("type mismatch: expected " + expectedValueClass.getName() + "; received: "
                    + firstAttributeValue.getClass().getName());
        Type value = (Type) firstAttributeValue;

        Attribute<Type> dataValue = new Attribute<Type>(name, value);
        return dataValue;
    }

    private Object convertFromXmlDatatypeToClientDatatype(Object value) {

        if (null == value)
            return null;
        Object result = value;
        if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar calendar = (XMLGregorianCalendar) value;
            result = calendar.toGregorianCalendar().getTime();
        }
        return result;
    }

    public void createAttribute(String userId, String attributeName, Object attributeValue)
            throws WSClientTransportException {

        targetIdentityHandler.setTargetIdentity(userId);

        CreateType create = new CreateType();
        List<CreateItemType> createItems = create.getCreateItem();
        CreateItemType createItem = new CreateItemType();
        createItems.add(createItem);

        createItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        AppDataType newData = new AppDataType();
        AttributeType attribute = new AttributeType();
        attribute.setName(attributeName);
        setAttributeValue(attributeValue, attribute, true);
        newData.setAttribute(attribute);
        createItem.setNewData(newData);

        try {
            CreateResponseType createResponse = port.create(create);
            StatusType status = createResponse.getStatus();
            LOG.debug("status: " + status.getCode());

            TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode.fromCode(status.getCode());
            if (TopLevelStatusCode.OK != topLevelStatusCode)
                throw new RuntimeException("error occurred while creating attribute");
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(location);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(port);
        }
    }

    /**
     * Sets the attribute value within the target SAML attribute element.
     * 
     * The input attribute value can be an Integer, Boolean or array of these in case of a multivalued attribute.
     * 
     * @param attributeValue
     * @param targetAttribute
     * @param newAttribute
     */
    private void setAttributeValue(Object attributeValue, AttributeType targetAttribute, boolean isNewAttribute) {

        if (null == attributeValue)
            return;
        List<Object> attributeValues = targetAttribute.getAttributeValue();
        if (CompoundUtil.isCompound(attributeValue)) {
            AttributeType compoundAttribute = createCompoundAttribute(attributeValue, isNewAttribute);
            attributeValues.add(compoundAttribute);
            return;
        }
        if (attributeValue.getClass().isArray()) {
            targetAttribute.getOtherAttributes().put(WebServiceConstants.MULTIVALUED_ATTRIBUTE, Boolean.TRUE.toString());
            int size = Array.getLength(attributeValue);
            for (int idx = 0; idx < size; idx++) {
                Object value = Array.get(attributeValue, idx);
                attributeValues.add(value);
            }
        } else {
            attributeValues.add(attributeValue);
        }
    }

    @SuppressWarnings("unchecked")
    private AttributeType createCompoundAttribute(Object attributeValue, boolean isNewAttribute) {

        Class attributeClass = attributeValue.getClass();
        Method[] methods = attributeClass.getMethods();
        AttributeType compoundAttribute = new AttributeType();
        Compound compoundAnnotation = (Compound) attributeClass.getAnnotation(Compound.class);
        String compoundName = compoundAnnotation.value();
        compoundAttribute.setName(compoundName);
        List<Object> attributeValues = compoundAttribute.getAttributeValue();
        LOG.debug("creating compound attribute: " + compoundName);
        String id = null;
        for (Method method : methods) {
            CompoundMember compoundMemberAnnotation = method.getAnnotation(CompoundMember.class);
            if (null != compoundMemberAnnotation) {
                String memberName = compoundMemberAnnotation.value();
                Object value;
                try {
                    value = method.invoke(attributeValue, new Object[] {});
                } catch (Exception e) {
                    throw new RuntimeException("could not get property: " + method.getName().substring(3));
                }
                AttributeType memberAttribute = new AttributeType();
                memberAttribute.setName(memberName);
                memberAttribute.getAttributeValue().add(value);
                attributeValues.add(memberAttribute);
            }
            CompoundId compoundIdAnnotation = method.getAnnotation(CompoundId.class);
            if (null != compoundIdAnnotation) {
                try {
                    id = (String) method.invoke(attributeValue, new Object[] {});
                } catch (Exception e) {
                    throw new RuntimeException("@Id property not of type string");
                }
            }
        }
        if (null != id) {
            compoundAttribute.getOtherAttributes().put(WebServiceConstants.COMPOUNDED_ATTRIBUTE_ID, id);
        } else if (false == isNewAttribute)
            /*
             * The @Id property is really required to be able to target the correct compound attribute record within the system. In case
             * we're creating a new compounded attribute record the attribute Id is of no use.
             */
            throw new IllegalArgumentException("Missing @Id property on compound attribute value");
        return compoundAttribute;
    }

    public void removeAttribute(String userIdn, String attributeName, String attributeId)
            throws WSClientTransportException {

        LOG.debug("remove attribute " + attributeName + " for subject " + userIdn);
        targetIdentityHandler.setTargetIdentity(userIdn);

        DeleteType delete = new DeleteType();
        List<DeleteItemType> deleteItems = delete.getDeleteItem();

        DeleteItemType deleteItem = new DeleteItemType();
        deleteItems.add(deleteItem);

        deleteItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        deleteItem.setSelect(select);

        if (null != attributeId) {
            select.getOtherAttributes().put(WebServiceConstants.COMPOUNDED_ATTRIBUTE_ID, attributeId);
        }

        DeleteResponseType deleteResponse;
        try {
            deleteResponse = port.delete(delete);
            StatusType status = deleteResponse.getStatus();
            LOG.debug("status: " + status.getCode());

            TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode.fromCode(status.getCode());
            if (TopLevelStatusCode.OK != topLevelStatusCode) {
                String comment = status.getComment();
                throw new RuntimeException("error occurred while removing attribute: " + comment);
            }
        } catch (ClientTransportException e) {
            throw new WSClientTransportException(location);
        } catch (Exception e) {
            throw retrieveHeadersFromException(e);
        } finally {
            retrieveHeadersFromPort(port);
        }
    }

    public <Type> void removeAttribute(String userId, Attribute<Type> attribute)
            throws WSClientTransportException {

        String attributeName = attribute.getName();

        Object value = attribute.getValue();
        if (CompoundUtil.isCompound(value)) {
            String attributeId = CompoundUtil.getAttributeId(value);
            removeAttribute(userId, attributeName, attributeId);
        } else {
            removeAttribute(userId, attributeName, null);
        }
    }
}
