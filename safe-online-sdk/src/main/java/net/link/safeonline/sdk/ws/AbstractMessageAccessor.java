/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Document;

/**
 * Abstract base implementation of the message accessor interface. Used by the
 * different web service client components.
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractMessageAccessor implements MessageAccessor {

	protected final MessageLoggerHandler messageLoggerHandler;
	private Map<String, Object> responseContext;

	public AbstractMessageAccessor() {
		this.messageLoggerHandler = new MessageLoggerHandler();
	}

	public Document getInboundMessage() {
		return this.messageLoggerHandler.getInboundMessage();
	}

	public Document getOutboundMessage() {
		return this.messageLoggerHandler.getOutboundMessage();
	}

	public boolean isCaptureMessages() {
		return this.messageLoggerHandler.isCaptureMessages();
	}

	public void setCaptureMessages(boolean captureMessages) {
		this.messageLoggerHandler.setCaptureMessages(captureMessages);
	}

	/**
	 * Registers the SOAP handler that this instance manages on the given JAX-WS
	 * port component.
	 * 
	 * @param port
	 */
	protected void registerMessageLoggerHandler(Object port) {
		BindingProvider bindingProvider = (BindingProvider) port;
		Binding binding = bindingProvider.getBinding();
		@SuppressWarnings("unchecked")
		List<Handler> handlerChain = binding.getHandlerChain();
		handlerChain.add(this.messageLoggerHandler);
		binding.setHandlerChain(handlerChain);
	}

	/**
	 * Call this method after your service request to set the response context
	 * of the response.<br>
	 * <br>
	 * For example:<br>
	 * <code>retrieveHeadersFromPort(port);</code>
	 */
	protected void retrieveHeadersFromPort(Object port) {

		if (!(port instanceof BindingProvider))
			throw new IllegalArgumentException(
					"Can only retrieve result HTTP headers from a JAX-WS proxy object.");

		this.responseContext = ((BindingProvider) port).getResponseContext();
	}

	/**
	 * {@inheritDoc}
	 */
	public LinkedList<Object> getHeader(String name) {

		return new LinkedList<Object>(getHeaders().get(name));
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<Object>> getHeaders() {

		return (Map<String, List<Object>>) responseContext
				.get(MessageContext.HTTP_RESPONSE_HEADERS);
	}
}
