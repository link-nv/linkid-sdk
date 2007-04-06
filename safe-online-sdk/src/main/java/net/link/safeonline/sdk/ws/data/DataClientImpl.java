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

import javax.xml.ws.BindingProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.client.ClientTransportException;

import liberty.dst._2006_08.ref.safe_online.DataService;
import liberty.dst._2006_08.ref.safe_online.DataServicePort;
import liberty.dst._2006_08.ref.safe_online.DataType;
import liberty.dst._2006_08.ref.safe_online.QueryItemType;
import liberty.dst._2006_08.ref.safe_online.QueryResponseType;
import liberty.dst._2006_08.ref.safe_online.QueryType;
import liberty.dst._2006_08.ref.safe_online.SelectType;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.data.ws.DataServiceFactory;
import net.link.safeonline.data.ws.SecondLevelStatusCode;
import net.link.safeonline.data.ws.TopLevelStatusCode;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.ApplicationAuthenticationUtils;

public class DataClientImpl implements DataClient {

	private static final Log LOG = LogFactory.getLog(DataClientImpl.class);

	private DataServicePort port;

	public DataClientImpl(String location, X509Certificate clientCertificate,
			PrivateKey clientPrivateKey) {
		DataService dataService = DataServiceFactory.newInstance();
		this.port = dataService.getDataServicePort();

		setEndpointAddress(location);

		ApplicationAuthenticationUtils.initWsSecurity(this.port,
				clientCertificate, clientPrivateKey);
	}

	private void setEndpointAddress(String location) {
		BindingProvider bindingProvider = (BindingProvider) this.port;

		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"https://" + location + "/safe-online-ws/data");
	}

	public String setAttributeValue(String subjectLogin, String attributeName,
			String attributeValue) throws ConnectException {

		try {
			this.port.modify(null);
		} catch (ClientTransportException e) {
			throw new ConnectException(e.getMessage());
		}

		return null;
	}

	public String getAttributeValue(String subjectLogin, String attributeName)
			throws ConnectException, RequestDeniedException {
		QueryType query = new QueryType();

		List<QueryItemType> queryItems = query.getQueryItem();
		QueryItemType queryItem = new QueryItemType();
		queryItems.add(queryItem);

		queryItem.setObjectType("StringAttribute");
		SelectType select = new SelectType();
		select.setUserId(subjectLogin);
		select.setAttributeName(attributeName);
		queryItem.setSelect(select);

		ApplicationAuthenticationUtils.configureSsl();

		QueryResponseType queryResponse;
		try {
			queryResponse = this.port.query(query);
		} catch (ClientTransportException e) {
			throw new ConnectException(e.getMessage());
		}

		StatusType status = queryResponse.getStatus();
		LOG.debug("status: " + queryResponse.getStatus().getCode());
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
				throw new IllegalStateException("entity does not exist");
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
			return null;
		}
		DataType data = dataList.get(0);
		String value = data.getValue();

		return value;
	}
}
