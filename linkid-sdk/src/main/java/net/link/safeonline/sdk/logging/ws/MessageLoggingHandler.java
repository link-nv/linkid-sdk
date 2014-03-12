/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.logging.ws;

import com.lyndir.lhunath.opal.system.logging.Logger;
import java.io.*;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Node;


/**
 * Log SOAP Handler. Will simply log the inbound and outbound SOAP messages. Can come in handy when debugging web services that run over
 * SSL.
 *
 * @author fcorneli
 */
public class MessageLoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger logger = Logger.get( MessageLoggingHandler.class );

    @Override
    public Set<QName> getHeaders() {

        return null;
    }

    @Override
    @SuppressWarnings("unused")
    public void close(MessageContext context) {

        // empty
    }

    @Override
    public boolean handleFault(SOAPMessageContext soapContext) {

        Boolean outboundProperty = (Boolean) soapContext.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY );
        SOAPMessage soapMessage = soapContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        logger.dbg( "SOAP fault (outbound: %s ): %s", outboundProperty, toString( soapPart ) );
        return true;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext soapContext) {

        Boolean outboundProperty = (Boolean) soapContext.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY );
        SOAPMessage soapMessage = soapContext.getMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        logger.dbg( "SOAP message (outbound: %s): %s", outboundProperty, toString( soapPart ) );
        try {
            File tmpFile;
            if (outboundProperty)
                tmpFile = File.createTempFile( "outbound-soap-", ".xml" );
            else
                tmpFile = File.createTempFile( "inbound-soap-", ".xml" );
            FileOutputStream outputStream = new FileOutputStream( tmpFile );
            try {
                IOUtils.write( toString( soapPart ), outputStream );
            }
            finally {
                IOUtils.closeQuietly( outputStream );
            }
        }
        catch (IOException e) {
            throw new RuntimeException( "IO error: " + e.getMessage() );
        }
        return true;
    }

    public static String toString(Node domNode) {

        Source source = new DOMSource( domNode );
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult( stringWriter );
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        /*
         * TransformerFactory nor Transformer are thread safe. Thus we cannot optimize via some @PostConstruct method to pre-create
         * instances of these.
         */
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            transformer.transform( source, result );
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException( "Transformer config error: " + e.getMessage() );
        }
        catch (TransformerException e) {
            throw new RuntimeException( "Transformer error: " + e.getMessage() );
        }
        return stringWriter.toString();
    }

    /**
     * Adds a new logging handler to the handler chain of the given JAX-WS port.
     */
    public static void addNewHandler(BindingProvider bindingProvider) {

        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> loggingHandler = new MessageLoggingHandler();
        handlerChain.add( loggingHandler );
        binding.setHandlerChain( handlerChain );
    }
}
