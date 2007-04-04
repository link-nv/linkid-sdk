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

import javax.xml.ws.BindingProvider;

import liberty.dst._2006_08.ref.DataService;
import liberty.dst._2006_08.ref.DataServicePort;
import net.link.safeonline.data.ws.DataServiceFactory;
import net.link.safeonline.sdk.ws.ApplicationAuthenticationUtils;

public class DataClientImpl implements DataClient {

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

		this.port.modify(null);

		return null;
	}

}
