/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.notification;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.w3c.dom.*;
import org.w3c.dom.Element;


public class NotificationConsumerTest {

    private static final Log LOG = LogFactory.getLog( NotificationConsumerTest.class );

    @Test
    public void notification()
            throws Exception {

        // Setup Data
        LOG.debug( "notify test" );

        ObjectFactory objectFactory = new ObjectFactory();
        Notify notify = objectFactory.createNotify();
        List<NotificationMessageHolderType> notifications = notify.getNotificationMessage();

        NotificationMessageHolderType notification = objectFactory.createNotificationMessageHolderType();
        TopicExpressionType topic = objectFactory.createTopicExpressionType();
        topic.setDialect( "http://docs.oasis-open.org/wsn/2004/06/TopicExpression/Simple" );
        notification.setTopic( topic );

        Message message = objectFactory.createNotificationMessageHolderTypeMessage();
        notification.setMessage( message );

        notifications.add( notification );

        JAXBContext context = JAXBContext.newInstance( ObjectFactory.class );
        Marshaller marshaller = context.createMarshaller();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // Test
        marshaller.marshal( notify, document );

        // Verify
        LOG.debug( "result document: " + domToString( document ) );

        Element nsElement = document.createElement( "nsElement" );
        nsElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:wsnt", "http://docs.oasis-open.org/wsn/b-2" );
        Node resultNode = XPathAPI.selectSingleNode( document, "/wsnt:Notify/wsnt:NotificationMessage/wsnt:Message", nsElement );
        assertNotNull( resultNode );

        Unmarshaller unmarshaller = context.createUnmarshaller();
        Notify notifyElement = (Notify) unmarshaller.unmarshal( document );

        assertNotNull( notifyElement.getNotificationMessage().get( 0 ).getMessage() );
    }

    public static String domToString(Node domNode)
            throws TransformerException {

        Source source = new DOMSource( domNode );
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult( stringWriter );
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        transformer.transform( source, result );
        return stringWriter.toString();
    }
}
