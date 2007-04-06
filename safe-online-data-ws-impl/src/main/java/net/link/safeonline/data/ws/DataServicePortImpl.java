/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.WebService;

import liberty.dst._2006_08.ref.safe_online.CreateResponseType;
import liberty.dst._2006_08.ref.safe_online.CreateType;
import liberty.dst._2006_08.ref.safe_online.DataServicePort;
import liberty.dst._2006_08.ref.safe_online.DataType;
import liberty.dst._2006_08.ref.safe_online.DeleteResponseType;
import liberty.dst._2006_08.ref.safe_online.DeleteType;
import liberty.dst._2006_08.ref.safe_online.ModifyResponseType;
import liberty.dst._2006_08.ref.safe_online.ModifyType;
import liberty.dst._2006_08.ref.safe_online.QueryItemType;
import liberty.dst._2006_08.ref.safe_online.QueryResponseType;
import liberty.dst._2006_08.ref.safe_online.QueryType;
import liberty.dst._2006_08.ref.safe_online.SelectType;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.AttributeProviderService;
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
@HandlerChain(file = "app-auth-ws-handlers.xml")
public class DataServicePortImpl implements DataServicePort {

	private static final Log LOG = LogFactory.getLog(DataServicePortImpl.class);

	private AttributeProviderService attributeProviderService;

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
		return null;
	}

	public DeleteResponseType delete(DeleteType request) {
		LOG.debug("delete");
		return null;
	}

	public ModifyResponseType modify(ModifyType request) {
		LOG.debug("modify");
		return null;
	}

	public static final String STRING_ATTRIBUTE_OBJECT_TYPE = "StringAttribute";

	public QueryResponseType query(QueryType request) {
		LOG.debug("query");
		List<QueryItemType> queryItems = request.getQueryItem();
		if (queryItems.size() > 1) {
			LOG.debug("query items > 1");
			QueryResponseType failedResponseType = createFailedQueryResponse(SecondLevelStatusCode.NO_MULTIPLE_ALLOWED);
			return failedResponseType;
		}
		QueryItemType queryItem = queryItems.get(0);
		if (null != queryItem.getCount()) {
			LOG.debug("pagination not supported");
			QueryResponseType failedResponseType = createFailedQueryResponse(SecondLevelStatusCode.PAGINATION_NOT_SUPPORTED);
			return failedResponseType;
		}
		String objectType = queryItem.getObjectType();
		if (false == STRING_ATTRIBUTE_OBJECT_TYPE.equals(objectType)) {
			LOG.debug("unsupported object type");
			QueryResponseType failedResponseType = createFailedQueryResponse(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE);
			return failedResponseType;
		}
		SelectType select = queryItem.getSelect();
		String userId = select.getUserId();
		String attributeName = select.getAttributeName();
		LOG.debug("query user " + userId + " for attribute " + attributeName);
		String value;
		try {
			value = this.attributeProviderService.getAttribute(userId,
					attributeName);
		} catch (AttributeTypeNotFoundException e) {
			QueryResponseType failedResponseType = createFailedQueryResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST,
					"AttributeTypeNotFound");
			return failedResponseType;
		} catch (PermissionDeniedException e) {
			QueryResponseType failedResponseType = createFailedQueryResponse(SecondLevelStatusCode.NOT_AUTHORIZED);
			return failedResponseType;
		} catch (SubjectNotFoundException e) {
			QueryResponseType failedResponseType = createFailedQueryResponse(
					SecondLevelStatusCode.DOES_NOT_EXIST, "SubjectNotFound");
			return failedResponseType;
		}
		QueryResponseType queryResponse = new QueryResponseType();
		StatusType status = new StatusType();
		status.setCode(TopLevelStatusCode.OK.getCode());
		queryResponse.setStatus(status);
		List<DataType> dataList = queryResponse.getData();
		if (null != value) {
			DataType data = new DataType();
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

	private QueryResponseType createFailedQueryResponse(
			SecondLevelStatusCode secondLevelStatusCode, String comment) {
		QueryResponseType failedQueryResponse = new QueryResponseType();
		StatusType status = new StatusType();
		status.setCode(TopLevelStatusCode.FAILED.getCode());
		failedQueryResponse.setStatus(status);
		if (null != secondLevelStatusCode) {
			List<StatusType> secondLevelStatuses = status.getStatus();
			StatusType secondLevelStatus = new StatusType();
			secondLevelStatus.setCode(secondLevelStatusCode.getCode());
			secondLevelStatus.setComment(comment);
			secondLevelStatuses.add(secondLevelStatus);
		}
		return failedQueryResponse;
	}
}
