/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.operation.saml2.request.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.xml.XMLObject;


/**
 * <h2>{@link AuthenticatedDeviceImpl}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 26, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AuthenticatedDeviceImpl extends AbstractSAMLObject implements AuthenticatedDevice {

    private String device;


    protected AuthenticatedDeviceImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /**
     * {@inheritDoc}
     */
    public String getDevice() {

        return device;
    }

    /**
     * {@inheritDoc}
     */
    public void setDevice(String device) {

        this.device = device;
    }

    /**
     * {@inheritDoc}
     */
    public List<XMLObject> getOrderedChildren() {

        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        return Collections.unmodifiableList(children);
    }

}
