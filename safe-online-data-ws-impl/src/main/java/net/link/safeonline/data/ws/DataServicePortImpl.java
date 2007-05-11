/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

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
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of data service using JAX-WS.
 * 
 * Specification: Liberty ID-WSF Data Service Template version 2.1
 * 
 * @author fcorneli
 * 
 */
@WebService(endpointInterface = "liberty.dst._2006_08.ref.safe_online.DataServicePort")
@HandlerChain(file = "data-ws-handlers.xml")
public class DataServicePortImpl implements DataServicePort {

	private static final Log LOG = LogFactory.getLog(DataServicePortImpl.class);

	private AttributeProviderService attributeProviderService;

	@Resource
	private WebServiceContext context;

	private AttributeProviderService getAttributeProviderService() {
		AttributeProviderService attributeProviderService = EjbUtils.getEJB(
				"SafeOnline/AttributeProviderServiceBean/local",
				AttributeProviderService.class);
		return attributeProviderService;
	}

	@PostConstruct
	public void postConstructCallback() {
		this.attributeProviderService = getAttributeProviderService();
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
		if (false == DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE
				.equals(objectType)) {
			LOG.debug("unsupported object type");
			CreateResponseType failedResponse = createFailedCreateResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
			return failedResponse;
		}

		String userId = TargetIdentityHandler.getTargetIdentity(this.context);

		AppDataType appData = createItem.getNewData();
		String attributeName = appData.getAttributeName();
		String attributeValue = appData.getAttributeValue();

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
		ModifyItemType modifyItem = modifyItems.get(0);

		String objectType = modifyItem.getObjectType();
		if (null == objectType) {
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.MISSING_OBJECT_TYPE);
			return failedResponse;
		}

		SelectType select = modifyItem.getSelect();
		String attributeName = select.getValue();
		String userId = TargetIdentityHandler.getTargetIdentity(this.context);
		AppDataType newData = modifyItem.getNewData();
		String encodedAttributeValue = newData.getAttributeValue();
		Object attributeValue;

		if (true == DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE
				.equals(objectType)) {
			attributeValue = encodedAttributeValue;
		} else if (true == DataServiceConstants.BOOLEAN_ATTRIBUTE_OBJECT_TYPE
				.equals(objectType)) {
			if (null == encodedAttributeValue) {
				attributeValue = null;
			} else {
				attributeValue = Boolean.parseBoolean(encodedAttributeValue);
			}
		} else {
			LOG.debug("unsupported object type: " + objectType);
			ModifyResponseType failedResponse = createFailedModifyResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
			return failedResponse;
		}

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
		if (false == DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE
				.equals(objectType)) {
			LOG.debug("unsupported object type");
			QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
			return failedResponse;
		}
		SelectType select = queryItem.getSelect();
		String userId = TargetIdentityHandler.getTargetIdentity(this.context);
		String attributeName = select.getValue();
		LOG.debug("query user " + userId + " for attribute " + attributeName);
		AttributeEntity attribute;
		try {
			attribute = this.attributeProviderService.findAttribute(userId,
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
		if (null != attribute) {
			DataType data = new DataType();
			String datatype = attribute.getAttributeType().getType();
			String encodedValue;
			if (SafeOnlineConstants.STRING_TYPE.equals(datatype)) {
				encodedValue = attribute.getStringValue();
			} else if (SafeOnlineConstants.BOOLEAN_TYPE.equals(datatype)) {
				Boolean booleanValue = attribute.getBooleanValue();
				/*
				 * 3VL booleans.
				 */
				if (null == booleanValue) {
					encodedValue = null;
				} else {
					encodedValue = Boolean.toString(booleanValue);
				}
			} else {
				QueryResponseType failedResponse = createFailedQueryResponse(SecondLevelStatusCode.INVALID_DATA);
				return failedResponse;
			}
			/*
			 * Notice that value can be null. In that case we send an empty Data
			 * element. No Data element means that the attribute provider still
			 * needs to create the attribute.
			 */
			data.setAttributeName(attributeName);
			data.setAttributeValue(encodedValue);
			dataList.add(data);
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
}
