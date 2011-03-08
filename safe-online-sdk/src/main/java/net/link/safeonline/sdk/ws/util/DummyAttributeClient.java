/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.sdk.ws.util;

import java.io.Serializable;
import java.util.*;
import net.link.safeonline.attribute.provider.AttributeSDK;
import net.link.safeonline.sdk.logging.exception.*;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import org.w3c.dom.Document;


/**
 * <h2>{@link DummyAttributeClient}<br> <sub>An linkID attribute service client used inside unit tests.</sub></h2>
 *
 * <p> [description / usage]. </p>
 *
 * <p> <i>Oct 9, 2008</i> </p>
 *
 * @author lhunath
 */
public class DummyAttributeClient implements AttributeClient {

    private static final Map<String, Map<String, List<AttributeSDK<?>>>> usersAttributes = new HashMap<String, Map<String, List<AttributeSDK<?>>>>();

    /**
     * Set an attribute for a given user to the given value.
     *
     * @return As per contract of {@link Map#put(Object, Object)}, this method returns the previous value of the given attribute, if one was
     *         set. Otherwise it returns <code>null</code>.
     */
    public static List<AttributeSDK<?>> setAttribute(String user, String attribute, Serializable... values) {

        Map<String, List<AttributeSDK<?>>> userAttributes = usersAttributes.get( user );
        if (userAttributes == null)
            usersAttributes.put( user, userAttributes = new HashMap<String, List<AttributeSDK<?>>>() );

        List<AttributeSDK<?>> attributes = new LinkedList<AttributeSDK<?>>();
        for (Serializable value : values) {
            attributes.add( new AttributeSDK( UUID.randomUUID().toString(), attribute, value ) );
        }

        return userAttributes.put( attribute, attributes );
    }

    /**
     * {@inheritDoc}
     */
    public static <T> T getAttributes(String userId, String attributeName, Class<T> valueClass)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException, AttributeUnavailableException {

        Object attributeValue = DummyAttributeClient.usersAttributes.get( userId ).get( attributeName );
        if (attributeValue == null || valueClass.isAssignableFrom( attributeValue.getClass() ))
            return valueClass.cast( attributeValue );

        throw new IllegalArgumentException(
                "Attribute '" + attributeName + "' of user id '" + userId + "' is not assignable to '" + valueClass + "'.  It is: "
                + attributeValue.getClass() + ": " + attributeValue );
    }

    /**
     * {@inheritDoc}
     */
    public void getAttributes(String userId, Map<String, List<AttributeSDK<?>>> attributes)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException, AttributeUnavailableException {

        attributes.clear();
        attributes.putAll( getAttributes( userId ) );
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<AttributeSDK<?>>> getAttributes(String userId)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException {

        return DummyAttributeClient.usersAttributes.get( userId );
    }

    public List<AttributeSDK<?>> getAttributes(final String userId, final String attributeName)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException {

        List<AttributeSDK<?>> result = DummyAttributeClient.usersAttributes.get( userId ).get( attributeName );
        if (null == result)
            return new LinkedList<AttributeSDK<?>>();
        else
            return DummyAttributeClient.usersAttributes.get( userId ).get( attributeName );
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getHeaders() {

        return new HashMap<String, List<String>>();
    }

    /**
     * {@inheritDoc}
     */
    public Document getInboundMessage() {

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Document getOutboundMessage() {

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCaptureMessages() {

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void setCaptureMessages(boolean captureMessages) {

        throw new UnsupportedOperationException();
    }
}
