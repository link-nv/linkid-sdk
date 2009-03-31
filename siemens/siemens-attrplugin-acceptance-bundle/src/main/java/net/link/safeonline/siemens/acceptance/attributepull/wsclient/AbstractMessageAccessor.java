/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.attributepull.wsclient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import net.link.safeonline.util.filter.ProfiledException;

import org.w3c.dom.Document;


/**
 * Abstract base implementation of the message accessor interface. Used by the different web service client components.
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractMessageAccessor implements MessageAccessor {

    protected final MessageLoggerHandler messageLoggerHandler;
    private Map<String, List<String>>    responseHeaders;


    public AbstractMessageAccessor() {

        messageLoggerHandler = new MessageLoggerHandler();
        responseHeaders = new HashMap<String, List<String>>();
    }

    public Document getInboundMessage() {

        return messageLoggerHandler.getInboundMessage();
    }

    public Document getOutboundMessage() {

        return messageLoggerHandler.getOutboundMessage();
    }

    public boolean isCaptureMessages() {

        return messageLoggerHandler.isCaptureMessages();
    }

    public void setCaptureMessages(boolean captureMessages) {

        messageLoggerHandler.setCaptureMessages(captureMessages);
    }

    /**
     * Registers the SOAP handler that this instance manages on the given JAX-WS port component.
     * 
     * @param port
     */
    protected void registerMessageLoggerHandler(Object port) {

        BindingProvider bindingProvider = (BindingProvider) port;
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        handlerChain.add(messageLoggerHandler);
        binding.setHandlerChain(handlerChain);
    }

    /**
     * Call this method after your service request to set the response context of the response.<br>
     * <br>
     * For example:<br>
     * <code>finally {
     *     retrieveHeadersFromPort(this.port);
     * }</code>
     */
    @SuppressWarnings("unchecked")
    protected void retrieveHeadersFromPort(Object port) {

        if (!(port instanceof BindingProvider))
            throw new IllegalArgumentException("Can only retrieve result HTTP headers from a JAX-WS proxy object.");

        Map<String, Object> context = ((BindingProvider) port).getResponseContext();
        if (context == null || !context.containsKey(MessageContext.HTTP_RESPONSE_HEADERS))
            return;

        responseHeaders = (Map<String, List<String>>) context.get(MessageContext.HTTP_RESPONSE_HEADERS);
    }

    /**
     * Call this method when your service request failed with a {@link ProfiledException}. This will extract the profile headers from the
     * exception..<br>
     * <br>
     * For example:<br>
     * <code>catch (ProfiledException e) {
     *     throw retrieveHeadersFromException(e);
     * }</code>
     */
    protected RuntimeException retrieveHeadersFromException(Exception e) {

        Throwable cause = e;

        if (e instanceof ProfiledException) {
            for (Map.Entry<String, String> header : ((ProfiledException) e).getHeaders().entrySet()) {
                responseHeaders.put(header.getKey(), Arrays.asList(new String[] { header.getValue() }));
            }

            // Throw the exception wrapped in the ProfiledException.
            cause = e.getCause();
        }

        if (cause instanceof RuntimeException)
            return (RuntimeException) cause;
        return new RuntimeException(cause);
    }

    public Map<String, List<String>> getHeaders() {

        return responseHeaders;
    }
}
