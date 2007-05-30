/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import java.net.ConnectException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

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
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.ApplicationAuthenticationUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;

public class DataClientImpl extends AbstractMessageAccessor implements
		DataClient {

	private static final Log LOG = LogFactory.getLog(DataClientImpl.class);

	private final DataServicePort port;

	private final TargetIdentityClientHandler targetIdentityHandler;

	/**
	 * Main constructor.
	 * 
	 * @param location
	 *            the location (i.e. host:port) of the data web service.
	 * @param clientCertificate
	 * @param clientPrivateKey
	 */
	public DataClientImpl(String location, X509Certificate clientCertificate,
			PrivateKey clientPrivateKey) {
		DataService dataService = DataServiceFactory.newInstance();
		AddressingFeature addressingFeature = new AddressingFeature();
		this.port = dataService.getDataServicePort(addressingFeature);

		setEndpointAddress(location);

		registerMessageLoggerHandler(this.port);

		/*
		 * The order of the JAX-WS handlers is important. For outbound messages
		 * the TargetIdentity SOAP handler needs to come first since it feeds
		 * additional XML Id's to be signed by the WS-Security handler.
		 */
		this.targetIdentityHandler = new TargetIdentityClientHandler();
		initTargetIdentityHandler();

		ApplicationAuthenticationUtils.initWsSecurity(this.port,
				clientCertificate, clientPrivateKey);
	}

	private void initTargetIdentityHandler() {
		BindingProvider bindingProvider = (BindingProvider) this.port;
		Binding binding = bindingProvider.getBinding();
		List<Handler> handlerChain = binding.getHandlerChain();
		handlerChain.add(this.targetIdentityHandler);
		binding.setHandlerChain(handlerChain);
	}

	private void setEndpointAddress(String location) {
		BindingProvider bindingProvider = (BindingProvider) this.port;

		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"https://" + location + "/safe-online-ws/data");
	}

	public void setAttributeValue(String subjectLogin, String attributeName,
			Object attributeValue) throws ConnectException {
		this.targetIdentityHandler.setTargetIdentity(subjectLogin);

		String encodedValue;
		String objectType;
		if (null == attributeValue) {
			encodedValue = null;
			objectType = DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE;
		} else if (true == attributeValue instanceof String) {
			encodedValue = (String) attributeValue;
			objectType = DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE;
		} else if (true == attributeValue instanceof Boolean) {
			encodedValue = Boolean.toString((Boolean) attributeValue);
			objectType = DataServiceConstants.BOOLEAN_ATTRIBUTE_OBJECT_TYPE;
		} else {
			throw new IllegalArgumentException(
					"attribute value type not supported");
		}

		ModifyType modify = new ModifyType();
		List<ModifyItemType> modifyItems = modify.getModifyItem();
		ModifyItemType modifyItem = new ModifyItemType();
		modifyItem.setObjectType(objectType);
		modifyItems.add(modifyItem);

		SelectType select = new SelectType();
		modifyItem.setSelect(select);
		select.setValue(attributeName);

		AppDataType newData = new AppDataType();
		modifyItem.setNewData(newData);
		newData.setAttributeName(attributeName);
		newData.setAttributeValue(encodedValue);

		ModifyResponseType modifyResponse;
		try {
			modifyResponse = this.port.modify(modify);
		} catch (ClientTransportException e) {
			throw new ConnectException(e.getMessage());
		}

		StatusType status = modifyResponse.getStatus();
		LOG.debug("status: " + modifyResponse.getStatus().getCode());
		TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode
				.fromCode(status.getCode());

		if (TopLevelStatusCode.OK != topLevelStatusCode) {
			List<StatusType> secondLevelStatusList = status.getStatus();
			if (secondLevelStatusList.size() > 0) {
				StatusType secondLevelStatus = secondLevelStatusList.get(0);
				LOG
						.debug("second level status: "
								+ secondLevelStatus.getCode());
				SecondLevelStatusCode secondLevelStatusCode = SecondLevelStatusCode
						.fromCode(secondLevelStatus.getCode());
				if (SecondLevelStatusCode.INVALID_DATA == secondLevelStatusCode) {
					throw new IllegalArgumentException(
							"attribute value type incorrect");
				}
			}
			LOG.debug("status comment: " + status.getComment());
			throw new RuntimeException("could not set the attribute");
		}
	}

	public DataValue getAttributeValue(String subjectLogin, String attributeName)
			throws ConnectException, RequestDeniedException,
			SubjectNotFoundException {
		this.targetIdentityHandler.setTargetIdentity(subjectLogin);

		QueryType query = new QueryType();

		List<QueryItemType> queryItems = query.getQueryItem();
		QueryItemType queryItem = new QueryItemType();
		queryItems.add(queryItem);

		queryItem
				.setObjectType(DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE);
		SelectType select = new SelectType();
		select.setValue(attributeName);
		queryItem.setSelect(select);

		ApplicationAuthenticationUtils.configureSsl();

		QueryResponseType queryResponse;
		try {
			queryResponse = this.port.query(query);
		} catch (ClientTransportException e) {
			throw new ConnectException(e.getMessage());
		}

		StatusType status = queryResponse.getStatus();
		LOG.debug("status: " + status.getCode());
		TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode
				.fromCode(status.getCode());
		switch (topLevelStatusCode) {
		case FAILED:
			List<StatusType> secondLevelStatuses = status.getStatus();
			if (0 == secondLevelStatuses.size()) {
				throw new RuntimeException("ID-WSF DST error");
			}
			StatusType secondLevelStatus = secondLevelStatuses.get(0);
			SecondLevelStatusCode secondLevelStatusCode = SecondLevelStatusCode
					.fromCode(secondLevelStatus.getCode());
			if (SecondLevelStatusCode.NOT_AUTHORIZED == secondLevelStatusCode) {
				throw new RequestDeniedException();
			}
			if (SecondLevelStatusCode.DOES_NOT_EXIST == secondLevelStatusCode) {
				throw new SubjectNotFoundException();
			}
			throw new RuntimeException("unknown error occurred");
		case OK:
			break;
		default:
			throw new RuntimeException("Unknown top level status code: "
					+ topLevelStatusCode);
		}

		List<DataType> dataList = queryResponse.getData();
		if (0 == dataList.size()) {
			LOG.debug("no data entry");
			return null;
		}
		DataType data = dataList.get(0);
		String value = data.getAttributeValue();
		String name = data.getAttributeName();

		DataValue dataValue = new DataValue(name, value);
		return dataValue;
	}

	public void createAttribute(String subjectLogin, String attributeName)
			throws ConnectException {

		this.targetIdentityHandler.setTargetIdentity(subjectLogin);

		CreateType create = new CreateType();
		List<CreateItemType> createItems = create.getCreateItem();
		CreateItemType createItem = new CreateItemType();
		createItems.add(createItem);

		createItem
				.setObjectType(DataServiceConstants.STRING_ATTRIBUTE_OBJECT_TYPE);
		AppDataType newData = new AppDataType();
		newData.setAttributeName(attributeName);
		createItem.setNewData(newData);

		CreateResponseType createResponse;
		try {
			createResponse = this.port.create(create);
		} catch (ClientTransportException e) {
			throw new ConnectException(e.getMessage());
		}

		StatusType status = createResponse.getStatus();
		LOG.debug("status: " + status.getCode());
		TopLevelStatusCode topLevelStatusCode = TopLevelStatusCode
				.fromCode(status.getCode());
		if (TopLevelStatusCode.OK != topLevelStatusCode) {
			throw new RuntimeException(
					"error occurred while creating attribute");
		}
	}
}
