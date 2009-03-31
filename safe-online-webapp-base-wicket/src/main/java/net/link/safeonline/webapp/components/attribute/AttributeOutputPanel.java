/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components.attribute;

import net.link.safeonline.data.AttributeDO;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


/**
 * <h2>{@link AttributeOutputPanel}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 26, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class AttributeOutputPanel extends Panel {

    private static final long  serialVersionUID = 1L;

    public static final String VALUE_LABEL_ID   = "value";


    public AttributeOutputPanel(String id, final AttributeDO attribute) {

        super(id);

        String value = null;
        if (null == attribute.getValueAsString() && attribute.isUnavailable()) {
            value = getLocalizer().getString("unavailable", this);
        } else if (null == attribute.getValueAsString()) {
            value = getLocalizer().getString("noValue", this);
        } else if (attribute.isCompounded()) {
            value = "";
        } else {
            value = attribute.getValueAsString();
        }

        add(new Label(VALUE_LABEL_ID, value));
    }
}
