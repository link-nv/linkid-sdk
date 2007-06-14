/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

import liberty.dst._2006_08.ref.safe_online.AppDataType;
import liberty.dst._2006_08.ref.safe_online.CreateItemType;
import liberty.dst._2006_08.ref.safe_online.CreateResponseType;
import liberty.dst._2006_08.ref.safe_online.CreateType;
import liberty.dst._2006_08.ref.safe_online.DataServicePort;
import liberty.dst._2006_08.ref.safe_online.DataType;
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
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.ws.common.WebServiceConstants;
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

	private static final Log LOG = LogFactory.getLog(DataServicePortImpl.class);

	@EJB(mappedName = "SafeOnline/AttributeProviderServiceBean/local")
	private AttributeProviderService attributeProviderService;

	@Resource
	private WebServiceContext context;

	@PostConstruct
	public void postConstructCallback() {
		LOG.debug("ready");
	}

	public CreateResponseType create(CreateType request) {
		LOG.debug("create");
		List<CreateItemType> createItems = request.getCreateItem();
		if (createItems.size() > 1) {
			CreateResponseType failedResponseType = createFailedCreateResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);
			return failedResponseType;
		}
		CreateItemType createItem = createItems.get(0);

		String objectType = createItem.getObjectType();
		if (null == objectType) {
			CreateResponseType failedResponse = createFailedCreateResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);
			return failedResponse;
		}
		if (false == DataServiceConstants.ATTRIBUTE_OBJECT_TYPE
				.equals(objectType)) {
			LOG.debug("unsupported object type: " + objectType);
			CreateResponseType failedResponse = createFailedCreateResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
			return failedResponse;
		}

		String userId;
		try {
			userId = TargetIdentityHandler.getTargetIdentity(this.context);
		} catch (TargetIdentityException e) {
			CreateResponseType failedResponse = createFailedCreateResponse(SecondLevelStatusCode.MISSING_CREDENTIALS);
			return failedResponse;
		}

		AppDataType appData = createItem.getNewData();
		AttributeType attribute = appData.getAttribute();
		String attributeName = attribute.getName();
		Object attributeValue = getValueObjectFromAttribute(attribute);

		try {
			this.attributeProviderService.createAttribute(userId,
					attributeName, attributeValue);
		} catch (SubjectNotFoundException e) {
			CreateResponseType failedResponse = createFailedCreateResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
			return failedResponse;
		} catch (AttributeTypeNotFoundException e) {
			CreateResponseType failedResponse = createFailedCreateResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST,
					"AttributeTypeNotFound");
			return failedResponse;
		} catch (PermissionDeniedException e) {
			CreateResponseType failedResponse = createFailedCreateResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
			return failedResponse;
		} catch (DatatypeMismatchException e) {
			CreateResponseType failedResponse = createFailedCreateResponse(SecondLevelStatusCode.INVALID_DATA);
			return failedResponse;
		}

		CreateResponseType createResponse = new CreateResponseType();
		StatusType status = new StatusType();
		status.setCode(TopLevelStatusCode.OK.getCode());
		createResponse.setStatus(status);
		return createResponse;
	}

	public DeleteResponseType delete(DeleteType request) {
		LOG.debug("delete: implement me");
		DeleteResponseType deleteResponse = new DeleteResponseType();
		StatusType status = new StatusType();
		status.setCode(TopLevelStatusCode.FAILED.getCode());
		status.setComment("delete operation not implemented");
		deleteResponse.setStatus(status);
		return deleteResponse;
	}

	public ModifyResponseType modify(ModifyType request) {
		LOG.debug("modify");
		List<ModifyItemType> modifyItems = request.getModifyItem();
		if (modifyItems.size() > 1) {
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);
			return failedResponse;
		}
		if (0 == modifyItems.size()) {
			ModifyResponseType failedResponse = createFailedModifyResponse(
					SecondLevelStatusCode.EMPTY_REQUEST, "missing ModifyItem");
			return failedResponse;
		}
		ModifyItemType modifyItem = modifyItems.get(0);

		String objectType = modifyItem.getObjectType();
		if (null == objectType) {
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);
			return failedResponse;
		}

		SelectType select = modifyItem.getSelect();
		if (null == select) {
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.MISSING_SELECT);
			return failedResponse;
		}
		String attributeName = select.getValue();
		String userId;
		try {
			userId = TargetIdentityHandler.getTargetIdentity(this.context);
		} catch (TargetIdentityException e) {
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.MISSING_CREDENTIALS);
			return failedResponse;
		}
		AppDataType newData = modifyItem.getNewData();
		if (null == newData) {
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.MISSING_NEW_DATA_ELEMENT);
			return failedResponse;
		}
		AttributeType attribute = newData.getAttribute();

		if (false == attributeName.equals(attribute.getName())) {
			/*
			 * Maybe we should drop the usage of Select all together.
			 */
			ModifyResponseType failedResponse = createFailedModifyResponse(
					SecondLevelStatusCode.INVALID_DATA,
					"Select and AttributeName do not correspond");
			return failedResponse;
		}

		if (false == DataServiceConstants.ATTRIBUTE_OBJECT_TYPE
				.equals(objectType)) {
			LOG.debug("unsupported object type: " + objectType);
			ModifyResponseType failedResponse = createFailedModifyResponse(
					SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE, objectType);
			return failedResponse;
		}

		Object attributeValue = getValueObjectFromAttribute(attribute);

		try {
			this.attributeProviderService.setAttribute(userId, attributeName,
					attributeValue);
		} catch (SubjectNotFoundException e) {
			ModifyResponseType failedResponse = createFailedModifyResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
			return failedResponse;
		} catch (AttributeTypeNotFoundException e) {
			ModifyResponseType failedResponse = createFailedModifyResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST,
					"AttributeTypeNotFound");
			return failedResponse;
		} catch (PermissionDeniedException e) {
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
			return failedResponse;
		} catch (AttributeNotFoundException e) {
			ModifyResponseType failedResponse = createFailedModifyResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST, "AttributeNotFound");
			return failedResponse;
		} catch (DatatypeMismatchException e) {
			ModifyResponseType failedResponse = createFailedModifyResponse(
					SecondLevelStatusCode.INVALID_DATA, "DatatypeMismatch");
			return failedResponse;
		}

		ModifyResponseType modifyResponse = new ModifyResponseType();
		StatusType status = new StatusType();
		status.setCode(TopLevelStatusCode.OK.getCode());
		modifyResponse.setStatus(status);
		return modifyResponse;
	}

	public QueryResponseType query(QueryType request) {
		LOG.debug("query");

		List<QueryItemType> queryItems = request.getQueryItem();
		if (queryItems.size() > 1) {
			LOG.debug("query items > 1");
			QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);
			return failedResponse;
		}
		if (0 == queryItems.size()) {
			QueryResponseType failedResponse = createFailedQueryResponse(
					SecondLevelStatusCode.EMPTY_REQUEST, "No Query Items");
			return failedResponse;
		}
		QueryItemType queryItem = queryItems.get(0);
		if (null != queryItem.getCount()) {
			LOG.debug("pagination not supported");
			QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.PAGINATION_NOT_SUPPORTED);
			return failedResponse;
		}
		String objectType = queryItem.getObjectType();
		if (null == objectType) {
			QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);
			return failedResponse;
		}
		if (false == DataServiceConstants.ATTRIBUTE_OBJECT_TYPE
				.equals(objectType)) {
			LOG.debug("unsupported object type: " + objectType);
			QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
			return failedResponse;
		}
		SelectType select = queryItem.getSelect();
		if (null == select) {
			QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.MISSING_SELECT);
			return failedResponse;
		}
		String userId;
		try {
			userId = TargetIdentityHandler.getTargetIdentity(this.context);
		} catch (TargetIdentityException e) {
			QueryResponseType failedResponse = createFailedQueryResponse(
					SecondLevelStatusCode.MISSING_CREDENTIALS,
					"no TargetIdentity found");
			return failedResponse;
		}
		String attributeName = select.getValue();
		LOG.debug("query user \"" + userId + "\" for attribute "
				+ attributeName);
		List<AttributeEntity> attributeList;
		try {
			attributeList = this.attributeProviderService.getAttributes(userId,
					attributeName);
		} catch (AttributeTypeNotFoundException e) {
			QueryResponseType failedResponse = createFailedQueryResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST,
					"AttributeTypeNotFound");
			return failedResponse;
		} catch (PermissionDeniedException e) {
			QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
			return failedResponse;
		} catch (SubjectNotFoundException e) {
			QueryResponseType failedResponse = createFailedQueryResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
			return failedResponse;
		}
		QueryResponseType queryResponse = new QueryResponseType();
		StatusType status = new StatusType();
		status.setCode(TopLevelStatusCode.OK.getCode());
		queryResponse.setStatus(status);
		List<DataType> dataList = queryResponse.getData();
		DataType data = new DataType();
		dataList.add(data);
		/*
		 * Notice that value can be null. In that case we send an empty Data
		 * element. No Data element means that the attribute provider still
		 * needs to create the attribute entity itself.
		 */
		if (false == attributeList.isEmpty()) {
			AttributeType attribute = new AttributeType();
			data.setAttribute(attribute);
			attribute.setName(attributeName);

			/*
			 * Communicate the 'type' meta-data via some XML attributes on the
			 * Attribute XML SAML element.
			 */
			AttributeTypeEntity attributeType = attributeList.get(0)
					.getAttributeType();
			if (attributeType.isMultivalued()) {
				Map<QName, String> otherAttributes = attribute
						.getOtherAttributes();
				otherAttributes.put(WebServiceConstants.MULTIVALUED_ATTRIBUTE,
						Boolean.TRUE.toString());
			}

			for (AttributeEntity attributeEntity : attributeList) {
				DatatypeType datatype = attributeEntity.getAttributeType()
						.getType();
				Object encodedValue;
				switch (datatype) {
				case STRING:
					encodedValue = attributeEntity.getStringValue();
					break;
				case BOOLEAN:
					encodedValue = attributeEntity.getBooleanValue();
					break;
				default:
					QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.INVALID_DATA);
					return failedResponse;
				}
				/*
				 * We're using http://www.w3.org/TR/xmlschema-1/#xsi_type here
				 * to communicate the datatype to the client.
				 */
				attribute.getAttributeValue().add(encodedValue);
			}
		}
		return queryResponse;
	}

	private QueryResponseType createFailedQueryResponse(
			SecondLevelStatusCode secondLevelStatusCode) {
		QueryResponseType failedQueryResponse = createFailedQueryResponse(
				secondLevelStatusCode, null);
		return failedQueryResponse;
	}

	private CreateResponseType createFailedCreateResponse(
			SecondLevelStatusCode secondLevelStatusCode, String comment) {
		CreateResponseType createResponse = new CreateResponseType();
		setResponseStatus(createResponse, secondLevelStatusCode, comment);
		return createResponse;
	}

	private ModifyResponseType createFailedModifyResponse(
			SecondLevelStatusCode secondLevelStatusCode, String comment) {
		ModifyResponseType modifyResponse = new ModifyResponseType();
		setResponseStatus(modifyResponse, secondLevelStatusCode, comment);
		return modifyResponse;
	}

	private ModifyResponseType createFailedModifyResponse(
			SecondLevelStatusCode secondLevelStatusCode) {
		ModifyResponseType modifyResponse = createFailedModifyResponse(
				secondLevelStatusCode, null);
		return modifyResponse;
	}

	private CreateResponseType createFailedCreateResponse(
			SecondLevelStatusCode secondLevelStatusCode) {
		CreateResponseType createResponse = createFailedCreateResponse(
				secondLevelStatusCode, null);
		return createResponse;
	}

	private QueryResponseType createFailedQueryResponse(
			SecondLevelStatusCode secondLevelStatusCode, String comment) {
		QueryResponseType failedQueryResponse = new QueryResponseType();
		setResponseStatus(failedQueryResponse, secondLevelStatusCode, comment);
		return failedQueryResponse;
	}

	private void setResponseStatus(ResponseType response,
			SecondLevelStatusCode secondLevelStatusCode, String comment) {
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
		if (attributeValues.isEmpty()) {
			return null;
		}
		if (false == Boolean.TRUE.toString().equals(
				attribute.getOtherAttributes().get(
						WebServiceConstants.MULTIVALUED_ATTRIBUTE))) {
			/*
			 * Single-valued attribute;
			 */
			return attributeValues.get(0);
		}
		/*
		 * Multivalued attribute.
		 */
		int size = attributeValues.size();
		/*
		 * We retrieve the component type for the array from the first attribute
		 * value element.
		 */
		Object firstAttributeValue = attributeValues.get(0);
		/*
		 * We're depending on xsi:type here to pass the type information from
		 * client to server.
		 */
		Class componentType = firstAttributeValue.getClass();
		Object result = Array.newInstance(componentType, size);
		for (int idx = 0; idx < size; idx++) {
			Array.set(result, idx, attributeValues.get(idx));
		}
		return result;
	}
}
