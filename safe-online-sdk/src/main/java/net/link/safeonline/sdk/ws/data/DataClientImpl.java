/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.data;

import java.lang.reflect.Array;
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
import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.exception.SubjectNotFoundException;
import net.link.safeonline.sdk.ws.AbstractMessageAccessor;
import net.link.safeonline.sdk.ws.SafeOnlineTrustManager;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.ws.common.WebServiceConstants;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

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

		WSSecurityClientHandler.addNewHandler(this.port,
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
			Object attributeValue) throws ConnectException,
			AttributeNotFoundException {
		this.targetIdentityHandler.setTargetIdentity(subjectLogin);

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
		if (attributeValue != null) {
			List<Object> attributeValues = attribute.getAttributeValue();
			if (attributeValue.getClass().isArray()) {
				attribute.getOtherAttributes().put(
						WebServiceConstants.MULTIVALUED_ATTRIBUTE,
						Boolean.TRUE.toString());
				int size = Array.getLength(attributeValue);
				for (int idx = 0; idx < size; idx++) {
					Object value = Array.get(attributeValue, idx);
					attributeValues.add(value);
				}
			} else {
				attributeValues.add(attributeValue);
			}
		}
		newData.setAttribute(attribute);

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
				switch (secondLevelStatusCode) {
				case INVALID_DATA:
					throw new IllegalArgumentException(
							"attribute value type incorrect");
				case DOES_NOT_EXIST:
					throw new AttributeNotFoundException();
				}
			}
			LOG.debug("status comment: " + status.getComment());
			throw new RuntimeException("could not set the attribute");
		}
	}

	@SuppressWarnings("unchecked")
	public <Type> Attribute<Type> getAttributeValue(String subjectLogin,
			String attributeName, Class<Type> expectedValueClass)
			throws ConnectException, RequestDeniedException,
			SubjectNotFoundException {
		this.targetIdentityHandler.setTargetIdentity(subjectLogin);

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
		AttributeType attribute = data.getAttribute();
		if (null == attribute) {
			/*
			 * This happens when the attribute entity does not exist.
			 */
			return null;
		}
		String name = attribute.getName();
		List<Object> attributeValues = attribute.getAttributeValue();
		Object firstAttributeValue = attributeValues.get(0);
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
			if (false == Boolean.TRUE.toString().equals(
					attribute.getOtherAttributes().get(
							WebServiceConstants.MULTIVALUED_ATTRIBUTE))) {
				String msg = "expected multivalued attribute, but received single-valued attribute";
				LOG.error(msg);
				throw new IllegalArgumentException(msg);
			}
			Class componentType = expectedValueClass.getComponentType();
			int size = attributeValues.size();
			Type values = (Type) Array.newInstance(componentType, size);
			for (int idx = 0; idx < size; idx++) {
				Object attributeValue = attributeValues.get(idx);
				if (false == componentType.isInstance(attributeValue)) {
					throw new IllegalArgumentException("expected type "
							+ componentType.getName() + "; received: "
							+ attributeValue.getClass().getName());
				}
				Array.set(values, idx, attributeValue);
			}
			Attribute<Type> resultAttribute = new Attribute<Type>(
					attributeName, values);
			return resultAttribute;
		} else {
			/*
			 * Single-valued attribute expected.
			 */
			if (false == expectedValueClass.isInstance(firstAttributeValue)) {
				throw new IllegalArgumentException("type mismatch: expected "
						+ expectedValueClass.getName() + "; received: "
						+ firstAttributeValue.getClass().getName());
			}
			Type value = (Type) attribute.getAttributeValue().get(0);

			Attribute<Type> dataValue = new Attribute<Type>(name, value);
			return dataValue;
		}
	}

	public void createAttribute(String subjectLogin, String attributeName)
			throws ConnectException {

		this.targetIdentityHandler.setTargetIdentity(subjectLogin);

		CreateType create = new CreateType();
		List<CreateItemType> createItems = create.getCreateItem();
		CreateItemType createItem = new CreateItemType();
		createItems.add(createItem);

		createItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
		AppDataType newData = new AppDataType();
		AttributeType attribute = new AttributeType();
		attribute.setName(attributeName);
		newData.setAttribute(attribute);
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
