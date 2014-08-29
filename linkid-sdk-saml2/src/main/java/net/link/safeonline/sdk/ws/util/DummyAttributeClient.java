/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.ws.util;

import java.io.Serializable;
import java.util.*;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.exception.*;
import net.link.safeonline.sdk.api.ws.attrib.AttributeClient;
import org.w3c.dom.Document;


/**
 * <h2>{@link DummyAttributeClient}<br> <sub>An linkID attribute service client used inside unit tests.</sub></h2>
 * <p/>
 * <p> [description / usage]. </p>
 * <p/>
 * <p> <i>Oct 9, 2008</i> </p>
 *
 * @author lhunath
 */
public class DummyAttributeClient implements AttributeClient {

    private static final Map<String, Map<String, List<AttributeSDK<Serializable>>>> usersAttributes = new HashMap<String, Map<String, List<AttributeSDK<Serializable>>>>();

    /**
     * Set an attribute for a given user to the given value.
     *
     * @return As per contract of {@link Map#put(Object, Object)}, this method returns the previous value of the given attribute, if one
     *         was
     *         set. Otherwise it returns {@code null}.
     */
    public static List<AttributeSDK<Serializable>> setAttribute(String user, String attribute, Serializable... values) {

        Map<String, List<AttributeSDK<Serializable>>> userAttributes = usersAttributes.get( user );
        if (userAttributes == null)
            usersAttributes.put( user, userAttributes = new HashMap<String, List<AttributeSDK<Serializable>>>() );

        List<AttributeSDK<Serializable>> attributes = new LinkedList<AttributeSDK<Serializable>>();
        for (Serializable value : values) {
            attributes.add( new AttributeSDK<Serializable>( UUID.randomUUID().toString(), attribute, value ) );
        }

        return userAttributes.put( attribute, attributes );
    }

    public static <T> T getAttributes(String userId, String attributeName, Class<T> valueClass)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException, AttributeUnavailableException {

        Object attributeValue = DummyAttributeClient.usersAttributes.get( userId ).get( attributeName );
        if (attributeValue == null || valueClass.isAssignableFrom( attributeValue.getClass() ))
            return valueClass.cast( attributeValue );

        throw new IllegalArgumentException(
                "Attribute '" + attributeName + "' of user id '" + userId + "' is not assignable to '" + valueClass + "'.  It is: "
                + attributeValue.getClass() + ": " + attributeValue );
    }

    @Override
    public void getAttributes(String userId, Map<String, List<AttributeSDK<Serializable>>> attributes)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException, AttributeUnavailableException {

        attributes.clear();
        attributes.putAll( getAttributes( userId ) );
    }

    @Override
    public Map<String, List<AttributeSDK<Serializable>>> getAttributes(String userId)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException {

        return DummyAttributeClient.usersAttributes.get( userId );
    }

    @Override
    public List<AttributeSDK<Serializable>> getAttributes(final String userId, final String attributeName)
            throws RequestDeniedException, WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException,
                   SubjectNotFoundException {

        List<AttributeSDK<Serializable>> result = DummyAttributeClient.usersAttributes.get( userId ).get( attributeName );
        if (null == result)
            return new LinkedList<AttributeSDK<Serializable>>();
        else
            return DummyAttributeClient.usersAttributes.get( userId ).get( attributeName );
    }

    public Map<String, List<String>> getHeaders() {

        return new HashMap<String, List<String>>();
    }

    public Document getInboundMessage() {

        throw new UnsupportedOperationException();
    }

    public Document getOutboundMessage() {

        throw new UnsupportedOperationException();
    }

    public boolean isCaptureMessages() {

        throw new UnsupportedOperationException();
    }

    public void setCaptureMessages(boolean captureMessages) {

        throw new UnsupportedOperationException();
    }
}
