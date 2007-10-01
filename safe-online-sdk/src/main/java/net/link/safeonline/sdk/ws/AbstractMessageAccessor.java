/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws;

import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.w3c.dom.Document;

public abstract class AbstractMessageAccessor implements MessageAccessor {

	protected final MessageLoggerHandler messageLoggerHandler;

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
}
