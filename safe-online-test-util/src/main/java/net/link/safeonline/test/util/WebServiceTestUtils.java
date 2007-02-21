/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.test.util;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 * Web Service Test Utils. Can be used to unit test JAX-WS endpoint
 * implementations.
 * 
 * @author fcorneli
 * 
 */
public class WebServiceTestUtils {

	private Endpoint endpoint;

	private HttpServer httpServer;

	private ExecutorService executorService;

	private int port;

	public void setUp(Object webServicePort) throws Exception {
		this.endpoint = Endpoint.create(webServicePort);

		this.httpServer = HttpServer.create();
		this.port = getFreePort();
		this.httpServer.bind(new InetSocketAddress(port), 5);
		this.executorService = Executors.newFixedThreadPool(5);
		this.httpServer.setExecutor(this.executorService);
		this.httpServer.start();

		HttpContext httpContext = httpServer.createContext("/test");
		endpoint.publish(httpContext);
	}

	public String getEndpointAddress() {
		String endpointAddress = "http://localhost:" + this.port + "/test";
		return endpointAddress;
	}

	public void tearDown() throws Exception {
		this.endpoint.stop();
		this.httpServer.stop(1);
		this.executorService.shutdown();
	}

	public void setEndpointAddress(Object webServiceClientPort) {
		BindingProvider bindingProvider = (BindingProvider) webServiceClientPort;
		String endpointAddress = getEndpointAddress();
		bindingProvider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
	}

	private static int getFreePort() throws Exception {
		ServerSocket serverSocket = new ServerSocket(0);
		int port = serverSocket.getLocalPort();
		serverSocket.close();
		return port;
	}
}
