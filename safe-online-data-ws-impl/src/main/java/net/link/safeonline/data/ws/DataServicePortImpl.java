/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

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
import liberty.util._2006_08.ResponseType;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.authentication.service.NodeAttributeService;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.CertificateDomainException;
import net.link.safeonline.ws.util.CertificateValidatorHandler;
import net.link.safeonline.ws.util.CertificateValidatorHandler.CertificateDomain;
import net.link.safeonline.ws.util.ri.Injection;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of data service using JAX-WS.
 * 
 * <p>
 * Specification: Liberty ID-WSF Data Service Template version 2.1
 * </p>
 * 
 * @author fcorneli
 * 
 */
@WebService(endpointInterface = "liberty.dst._2006_08.ref.safe_online.DataServicePort")
@HandlerChain(file = "data-ws-handlers.xml")
@Addressing
@Injection
public class DataServicePortImpl implements DataServicePort {

    private static final Log         LOG = LogFactory.getLog(DataServicePortImpl.class);

    @EJB(mappedName = AttributeProviderService.JNDI_BINDING)
    private AttributeProviderService attributeProviderService;

    @EJB(mappedName = NodeAttributeService.JNDI_BINDING)
    private NodeAttributeService     nodeAttributeService;

    @Resource
    private WebServiceContext        context;

    private CertificateDomain        certificateDomain;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        LOG.debug("postConstruct");

        LOG.debug("ready");
    }

    public CreateResponseType create(CreateType request) {

        LOG.debug("create");

        try {
            certificateDomain = CertificateValidatorHandler.getCertificateDomain(context);
        } catch (CertificateDomainException e) {
            return createFailedCreateResponse(SecondLevelStatusCode.INVALID_DATA);
        }

        List<CreateItemType> createItems = request.getCreateItem();
        if (createItems.size() > 1)
            return createFailedCreateResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);
        CreateItemType createItem = createItems.get(0);

        String objectType = createItem.getObjectType();
        if (null == objectType)
            return createFailedCreateResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);

        if (false == DataServiceConstants.ATTRIBUTE_OBJECT_TYPE.equals(objectType)) {
            LOG.debug("unsupported object type: " + objectType);
            return createFailedCreateResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
        }

        try {
            String userId = TargetIdentityHandler.getTargetIdentity(context);
            AppDataType appData = createItem.getNewData();
            AttributeType attribute = appData.getAttribute();
            String attributeName = attribute.getName();

            Object attributeValue;
            if (isCompoundAttribute(attribute)) {
                attributeValue = getCompoundMemberValues(attribute);
            } else {
                attributeValue = getValueObjectFromAttribute(attribute);
            }

            if (certificateDomain.equals(CertificateDomain.APPLICATION)) {
                attributeProviderService.createAttribute(userId, attributeName, attributeValue);
            } else if (certificateDomain.equals(CertificateDomain.NODE)) {
                nodeAttributeService.createAttribute(userId, attributeName, attributeValue);
            } else
                return createFailedCreateResponse(SecondLevelStatusCode.NOT_AUTHORIZED);

            StatusType status = new StatusType();
            status.setCode(TopLevelStatusCode.OK.getCode());
            CreateResponseType createResponse = new CreateResponseType();
            createResponse.setStatus(status);

            return createResponse;
        }

        catch (TargetIdentityException e) {
            return createFailedCreateResponse(SecondLevelStatusCode.MISSING_CREDENTIALS);
        } catch (SubjectNotFoundException e) {
            return createFailedCreateResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
        } catch (AttributeTypeNotFoundException e) {
            return createFailedCreateResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeTypeNotFound");
        } catch (PermissionDeniedException e) {
            return createFailedCreateResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
        } catch (DatatypeMismatchException e) {
            return createFailedCreateResponse(SecondLevelStatusCode.INVALID_DATA);
        } catch (NodeNotFoundException e) {
            return createFailedCreateResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "NodeNotFound");
        }
    }

    public DeleteResponseType delete(DeleteType request) {

        LOG.debug("delete");

        try {
            certificateDomain = CertificateValidatorHandler.getCertificateDomain(context);
        } catch (CertificateDomainException e) {
            return createFailedDeleteResponse(SecondLevelStatusCode.INVALID_DATA);
        }

        List<DeleteItemType> deleteItems = request.getDeleteItem();
        if (deleteItems.size() > 1)
            return createFailedDeleteResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);
        DeleteItemType deleteItem = deleteItems.get(0);

        String objectType = deleteItem.getObjectType();
        if (null == objectType)
            return createFailedDeleteResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);

        if (false == DataServiceConstants.ATTRIBUTE_OBJECT_TYPE.equals(objectType)) {
            LOG.debug("unsupported object type: " + objectType);
            return createFailedDeleteResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
        }

        SelectType select = deleteItem.getSelect();
        if (null == select) {
            DeleteResponseType failedResponse = createFailedDeleteResponse(SecondLevelStatusCode.MISSING_SELECT);
            return failedResponse;
        }
        String attributeName = select.getValue();

        try {
            String userId = TargetIdentityHandler.getTargetIdentity(context);
            String attributeId = select.getOtherAttributes().get(WebServiceConstants.COMPOUNDED_ATTRIBUTE_ID);

            if (certificateDomain.equals(CertificateDomain.APPLICATION)) {
                if (null == attributeId) {
                    attributeProviderService.removeAttribute(userId, attributeName);
                } else {
                    attributeProviderService.removeCompoundAttributeRecord(userId, attributeName, attributeId);
                }
            } else if (certificateDomain.equals(CertificateDomain.NODE)) {
                if (null == attributeId) {
                    nodeAttributeService.removeAttribute(userId, attributeName);
                } else {
                    nodeAttributeService.removeCompoundAttributeRecord(userId, attributeName, attributeId);
                }
            } else
                return createFailedDeleteResponse(SecondLevelStatusCode.NOT_AUTHORIZED);

            StatusType status = new StatusType();
            status.setCode(TopLevelStatusCode.OK.getCode());
            DeleteResponseType deleteResponse = new DeleteResponseType();
            deleteResponse.setStatus(status);

            return deleteResponse;
        }

        catch (TargetIdentityException e) {
            return createFailedDeleteResponse(SecondLevelStatusCode.MISSING_CREDENTIALS);
        } catch (SubjectNotFoundException e) {
            return createFailedDeleteResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
        } catch (AttributeTypeNotFoundException e) {
            return createFailedDeleteResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeTypeNotFound");
        } catch (PermissionDeniedException e) {
            return createFailedDeleteResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
        } catch (AttributeNotFoundException e) {
            return createFailedDeleteResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeNotFound");
        } catch (NodeNotFoundException e) {
            return createFailedDeleteResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "NodeNotFound");
        }
    }

    public ModifyResponseType modify(ModifyType request) {

        LOG.debug("modify");

        try {
            certificateDomain = CertificateValidatorHandler.getCertificateDomain(context);
        } catch (CertificateDomainException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.INVALID_DATA);
        }

        List<ModifyItemType> modifyItems = request.getModifyItem();
        if (modifyItems.size() > 1)
            return createFailedModifyResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);

        if (0 == modifyItems.size())
            return createFailedModifyResponse(SecondLevelStatusCode.EMPTY_REQUEST, "missing ModifyItem");
        ModifyItemType modifyItem = modifyItems.get(0);

        String objectType = modifyItem.getObjectType();
        if (null == objectType)
            return createFailedModifyResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);

        if (false == DataServiceConstants.ATTRIBUTE_OBJECT_TYPE.equals(objectType)) {
            LOG.debug("unsupported object type: " + objectType);
            return createFailedModifyResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE, objectType);
        }

        SelectType select = modifyItem.getSelect();
        if (null == select)
            return createFailedModifyResponse(SecondLevelStatusCode.MISSING_SELECT);

        AppDataType newData = modifyItem.getNewData();
        if (null == newData)
            return createFailedModifyResponse(SecondLevelStatusCode.MISSING_NEW_DATA_ELEMENT);

        String attributeName = select.getValue();
        AttributeType attribute = newData.getAttribute();
        if (false == attributeName.equals(attribute.getName()))
            /* TODO: Maybe we should drop the usage of Select all together. */
            return createFailedModifyResponse(SecondLevelStatusCode.INVALID_DATA, "Select and AttributeName do not correspond");

        try {
            String userId = TargetIdentityHandler.getTargetIdentity(context);

            /*
             * Different strategies are possible to go from the SAML attribute to the AttributeProviderService attribute data object. Here
             * we have chosen not to multiplex the SAML attribute to a generic attribute data object when invoking the setAttribute. Let's
             * just call a setAttribute method dedicated for compounded attribute records.
             */
            if (isCompoundAttribute(attribute)) {
                String attributeId = findAttributeId(attribute);
                if (null == attributeId)
                    return createFailedModifyResponse(SecondLevelStatusCode.INVALID_DATA, "AttributeId required");

                Map<String, Object> memberValues = getCompoundMemberValues(attribute);

                if (certificateDomain.equals(CertificateDomain.APPLICATION)) {
                    attributeProviderService.setCompoundAttributeRecord(userId, attributeName, attributeId, memberValues);
                } else if (certificateDomain.equals(CertificateDomain.NODE)) {
                    nodeAttributeService.setCompoundAttributeRecord(userId, attributeName, attributeId, memberValues);
                } else
                    return createFailedModifyResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
            }

            else {
                Object attributeValue = getValueObjectFromAttribute(attribute);

                if (certificateDomain.equals(CertificateDomain.APPLICATION)) {
                    attributeProviderService.setAttribute(userId, attributeName, attributeValue);
                } else if (certificateDomain.equals(CertificateDomain.NODE)) {
                    nodeAttributeService.setAttribute(userId, attributeName, attributeValue);
                } else
                    return createFailedModifyResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
            }

            StatusType status = new StatusType();
            status.setCode(TopLevelStatusCode.OK.getCode());
            ModifyResponseType modifyResponse = new ModifyResponseType();
            modifyResponse.setStatus(status);

            return modifyResponse;
        }

        catch (TargetIdentityException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.MISSING_CREDENTIALS);
        } catch (SubjectNotFoundException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
        } catch (AttributeTypeNotFoundException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeTypeNotFound");
        } catch (PermissionDeniedException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
        } catch (DatatypeMismatchException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.INVALID_DATA, "DatatypeMismatch");
        } catch (AttributeNotFoundException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeNotFound");
        } catch (NodeNotFoundException e) {
            return createFailedModifyResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "NodeNotFound");
        }
    }

    private Map<String, Object> getCompoundMemberValues(AttributeType attribute) {

        AttributeType compoundAttribute = (AttributeType) attribute.getAttributeValue().get(0);
        Map<String, Object> compoundMemberValues = new HashMap<String, Object>();

        for (Object attributeValue : compoundAttribute.getAttributeValue()) {
            AttributeType memberAttribute = (AttributeType) attributeValue;

            List<Object> memberValues = memberAttribute.getAttributeValue();
            String memberName = memberAttribute.getName();
            Object memberValue = memberValues.isEmpty()? null: memberValues.get(0);

            compoundMemberValues.put(memberName, convertXMLDatatypeToServiceDatatype(memberValue));
        }

        return compoundMemberValues;
    }

    private String findAttributeId(AttributeType attribute) {

        AttributeType compoundAttribute = (AttributeType) attribute.getAttributeValue().get(0);

        return compoundAttribute.getOtherAttributes().get(WebServiceConstants.COMPOUNDED_ATTRIBUTE_ID);
    }

    private boolean isCompoundAttribute(AttributeType attribute) {

        List<Object> attributeValues = attribute.getAttributeValue();
        if (attributeValues.isEmpty())
            return false;

        Object attributeValue = attributeValues.get(0);
        if (attributeValue instanceof AttributeType)
            return true;

        return false;
    }

    @SuppressWarnings("unchecked")
    public QueryResponseType query(QueryType request) {

        LOG.debug("query");

        try {
            certificateDomain = CertificateValidatorHandler.getCertificateDomain(context);
        } catch (CertificateDomainException e) {
            return createFailedQueryResponse(SecondLevelStatusCode.INVALID_DATA);
        }

        List<QueryItemType> queryItems = request.getQueryItem();
        if (queryItems.size() > 1)
            return createFailedQueryResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);

        if (0 == queryItems.size())
            return createFailedQueryResponse(SecondLevelStatusCode.EMPTY_REQUEST, "No Query Items");

        QueryItemType queryItem = queryItems.get(0);
        if (null != queryItem.getCount())
            return createFailedQueryResponse(SecondLevelStatusCode.PAGINATION_NOT_SUPPORTED);

        String objectType = queryItem.getObjectType();
        if (null == objectType)
            return createFailedQueryResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);

        if (false == DataServiceConstants.ATTRIBUTE_OBJECT_TYPE.equals(objectType))
            return createFailedQueryResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);

        SelectType select = queryItem.getSelect();
        if (null == select)
            return createFailedQueryResponse(SecondLevelStatusCode.MISSING_SELECT);

        try {
            String userId = TargetIdentityHandler.getTargetIdentity(context);
            String attributeName = select.getValue();
            LOG.debug("query user \"" + userId + "\" for attribute " + attributeName);

            Object attributeValues = null;
            if (certificateDomain.equals(CertificateDomain.APPLICATION)) {
                attributeValues = attributeProviderService.getAttributes(userId, attributeName);
            } else if (certificateDomain.equals(CertificateDomain.NODE)) {
                attributeValues = nodeAttributeService.getAttributes(userId, attributeName);
            } else
                return createFailedQueryResponse(SecondLevelStatusCode.NOT_AUTHORIZED);

            StatusType status = new StatusType();
            status.setCode(TopLevelStatusCode.OK.getCode());
            QueryResponseType queryResponse = new QueryResponseType();
            queryResponse.setStatus(status);

            List<DataType> dataList = queryResponse.getData();
            DataType data = new DataType();
            dataList.add(data);

            /*
             * Notice that value can be null. In that case we send an empty Data element. No Data element means that the attribute provider
             * still needs to create the attribute entity itself.
             */
            if (null != attributeValues) {
                AttributeType attribute = new AttributeType();
                data.setAttribute(attribute);
                attribute.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
                attribute.setName(attributeName);

                /*
                 * Communicate the 'type' meta-data via some XML attributes on the Attribute XML SAML element.
                 */
                if (attributeValues.getClass().isArray()) {
                    Map<QName, String> otherAttributes = attribute.getOtherAttributes();
                    otherAttributes.put(WebServiceConstants.MULTIVALUED_ATTRIBUTE, Boolean.TRUE.toString());

                    for (Object attributeValue : (Object[]) attributeValues) {
                        if (attributeValue instanceof Map) {
                            // compound
                            Map<String, Object> attributeMap = (Map<String, Object>) attributeValue;

                            AttributeType compoundAttribute = new AttributeType();
                            compoundAttribute.setNameFormat(WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC);
                            compoundAttribute.setName(attributeName);
                            // add compound parent id
                            compoundAttribute.getOtherAttributes().put(WebServiceConstants.COMPOUNDED_ATTRIBUTE_ID,
                                    (String) attributeMap.get(attributeName));
                            // add compound members
                            List<Object> memberAttributeValues = compoundAttribute.getAttributeValue();
                            for (Entry<String, Object> attributeMapEntry : attributeMap.entrySet()) {
                                AttributeType memberAttribute = new AttributeType();
                                memberAttribute.setNameFormat(WebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE);
                                memberAttribute.setName(attributeMapEntry.getKey());

                                memberAttribute.getAttributeValue().add(attributeMapEntry.getValue());
                                memberAttributeValues.add(memberAttribute);
                            }

                            attribute.getAttributeValue().add(compoundAttribute);
                        } else {
                            // non-compound multivalued
                            attribute.getAttributeValue().add(attributeValue);
                        }
                    }

                } else {
                    // single-valued
                    attribute.getAttributeValue().add(attributeValues);
                }
            }
            return queryResponse;
        }

        catch (TargetIdentityException e) {
            QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.MISSING_CREDENTIALS,
                    "no TargetIdentity found");
            return failedResponse;
        } catch (AttributeTypeNotFoundException e) {
            QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeTypeNotFound");
            return failedResponse;
        } catch (PermissionDeniedException e) {
            QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
            return failedResponse;
        } catch (SubjectNotFoundException e) {
            QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
            return failedResponse;
        } catch (EJBException e) {
            return createFailedQueryResponse(SecondLevelStatusCode.INVALID_DATA);
        } catch (AttributeUnavailableException e) {
            QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeUnavailable");
            return failedResponse;
        }
    }

    private QueryResponseType createFailedQueryResponse(SecondLevelStatusCode secondLevelStatusCode) {

        return createFailedQueryResponse(secondLevelStatusCode, null);
    }

    private CreateResponseType createFailedCreateResponse(SecondLevelStatusCode secondLevelStatusCode, String comment) {

        CreateResponseType createResponse = new CreateResponseType();
        setResponseStatus(createResponse, secondLevelStatusCode, comment);

        return createResponse;
    }

    private DeleteResponseType createFailedDeleteResponse(SecondLevelStatusCode secondLevelStatusCode, String comment) {

        DeleteResponseType deleteResponse = new DeleteResponseType();
        setResponseStatus(deleteResponse, secondLevelStatusCode, comment);

        return deleteResponse;
    }

    private DeleteResponseType createFailedDeleteResponse(SecondLevelStatusCode secondLevelStatusCode) {

        return createFailedDeleteResponse(secondLevelStatusCode, null);
    }

    private ModifyResponseType createFailedModifyResponse(SecondLevelStatusCode secondLevelStatusCode, String comment) {

        ModifyResponseType modifyResponse = new ModifyResponseType();
        setResponseStatus(modifyResponse, secondLevelStatusCode, comment);

        return modifyResponse;
    }

    private ModifyResponseType createFailedModifyResponse(SecondLevelStatusCode secondLevelStatusCode) {

        return createFailedModifyResponse(secondLevelStatusCode, null);
    }

    private CreateResponseType createFailedCreateResponse(SecondLevelStatusCode secondLevelStatusCode) {

        return createFailedCreateResponse(secondLevelStatusCode, null);
    }

    private QueryResponseType createFailedQueryResponse(SecondLevelStatusCode secondLevelStatusCode, String comment) {

        QueryResponseType failedQueryResponse = new QueryResponseType();
        setResponseStatus(failedQueryResponse, secondLevelStatusCode, comment);

        return failedQueryResponse;
    }

    private void setResponseStatus(ResponseType response, SecondLevelStatusCode secondLevelStatusCode, String comment) {

        StatusType status = new StatusType();
        status.setCode(TopLevelStatusCode.FAILED.getCode());
        response.setStatus(status);

        if (null != secondLevelStatusCode) {
            List<StatusType> secondLevelStatuses = status.getStatus();

            StatusType secondLevelStatus = new StatusType();
            secondLevelStatus.setCode(secondLevelStatusCode.getCode());
            secondLevelStatus.setComment(comment);
            secondLevelStatuses.add(secondLevelStatus);
        }
    }

    private Object getValueObjectFromAttribute(AttributeType attribute) {

        List<Object> attributeValues = attribute.getAttributeValue();
        if (attributeValues.isEmpty())
            return null;

        if (false == Boolean.parseBoolean(attribute.getOtherAttributes().get(WebServiceConstants.MULTIVALUED_ATTRIBUTE)))
            // Single-valued attribute
            return convertXMLDatatypeToServiceDatatype(attributeValues.get(0));

        // Multivalued attribute.
        // We retrieve the component type for the array from the first attribute value element.
        Object firstAttributeValue = convertXMLDatatypeToServiceDatatype(attributeValues.get(0));
        int size = attributeValues.size();

        // We're depending on xsi:type here to pass the type information from client to server.
        Class<?> componentType = firstAttributeValue.getClass();
        Object valuesArray = Array.newInstance(componentType, size);
        for (int i = 0; i < size; ++i) {
            Array.set(valuesArray, i, convertXMLDatatypeToServiceDatatype(attributeValues.get(i)));
        }

        return valuesArray;
    }

    /**
     * Converter to go from XML datatypes to Service datatypes. The Service layer doesn't eat XMLGregorianCalendars.
     * 
     * @param value
     */
    private Object convertXMLDatatypeToServiceDatatype(Object value) {

        if (null == value)
            return null;

        if (value instanceof XMLGregorianCalendar) {
            XMLGregorianCalendar calendar = (XMLGregorianCalendar) value;
            return calendar.toGregorianCalendar().getTime();
        }

        return value;
    }
}
