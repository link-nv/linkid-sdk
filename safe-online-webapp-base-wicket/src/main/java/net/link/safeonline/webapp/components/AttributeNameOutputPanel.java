/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.components;

import net.link.safeonline.data.AttributeDO;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


/**
 * <h2>{@link AttributeNameOutputPanel}<br>
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
public class AttributeNameOutputPanel extends Panel {

    private static final long  serialVersionUID = 1L;

    public static final String NAME_LABEL_ID    = "name";


    public AttributeNameOutputPanel(String id, final AttributeDO attribute) {

        super(id);

        String name = attribute.getHumanReadableName();
        if (null == name) {
            name = attribute.getName();
        }
        Label nameLabel = new Label(NAME_LABEL_ID, name);
        if (attribute.isCompounded()) {
            nameLabel.add(new SimpleAttributeModifier("class", "so-nameoutput-compounded"));
        } else if (attribute.isMember()) {
            nameLabel.add(new SimpleAttributeModifier("class", "so-nameoutput-member"));
        }
        add(nameLabel);
    }
}
