/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.tools.olas;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.link.safeonline.sdk.exception.AttributeNotFoundException;
import net.link.safeonline.sdk.exception.AttributeUnavailableException;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.CompoundUtil;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.annotation.IdentityAttribute;
import net.link.safeonline.sdk.ws.attrib.annotation.IdentityCard;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;

import org.w3c.dom.Document;


/**
 * <h2>{@link DummyAttributeClient}<br>
 * <sub>An OLAS attribute service client used inside unit tests.</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 9, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class DummyAttributeClient implements AttributeClient {

    private final Map<String, Map<String, Object>> usersAttributes;


    public DummyAttributeClient() {

        this.usersAttributes = new HashMap<String, Map<String, Object>>();
        Map<String, Object> userAttributes = new HashMap<String, Object>();

        userAttributes.clear();
        userAttributes.put("urn:net:lin-k:safe-online:attribute:login", "Administrator");
        this.usersAttributes.put("admin", userAttributes);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getAttributeValue(String userId, String attributeName, Class<T> valueClass)
            throws AttributeNotFoundException, RequestDeniedException, WSClientTransportException,
            AttributeUnavailableException {

        return valueClass.cast(this.usersAttributes.get(userId).get(attributeName));
    }

    /**
     * {@inheritDoc}
     */
    public void getAttributeValues(String userId, Map<String, Object> attributes) throws AttributeNotFoundException,
            RequestDeniedException, WSClientTransportException, AttributeUnavailableException {

        attributes.clear();
        attributes.putAll(getAttributeValues(userId));
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getAttributeValues(String userId) throws RequestDeniedException,
            WSClientTransportException, AttributeNotFoundException, AttributeUnavailableException {

        return this.usersAttributes.get(userId);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getIdentity(String userId, Class<T> identityCardClass) throws AttributeNotFoundException,
            RequestDeniedException, WSClientTransportException, AttributeUnavailableException {

        if (!identityCardClass.isAnnotationPresent(IdentityCard.class))
            throw new IllegalArgumentException("identity card class should be annotated with @IdentityCard");

        try {
            T identityCard = identityCardClass.newInstance();
            Method[] methods = identityCardClass.getMethods();
            
            for (Method method : methods) {
                if (!method.isAnnotationPresent(IdentityAttribute.class)) {
                    continue;
                }

                String attributeName = method.getAnnotation(IdentityAttribute.class).value();
                Class<?> valueClass = method.getReturnType();
                Object attributeValue = getAttributeValue(userId, attributeName, valueClass);
                Method setMethod = CompoundUtil.getSetMethod(identityCardClass, method);
                
                try {
                    setMethod.invoke(identityCard, new Object[] { attributeValue });
                } catch (Exception e) {
                    throw new RuntimeException("error: " + e.getMessage());
                }
            }
            
            return identityCard;
        }

        catch (Exception e) {
            throw new RuntimeException("could not instantiate the identity card class");
        }
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
