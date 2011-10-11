/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider.confirmation;

import net.link.safeonline.attribute.provider.AttributeCore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;


public class DefaultAttributeConfirmationPanel extends AttributeConfirmationPanel {

    private static final Log LOG = LogFactory.getLog( DefaultAttributeConfirmationPanel.class );

    public static final String ACTION_ID = "action";

    Label component;

    public DefaultAttributeConfirmationPanel(String id, String confirmationId, final AttributeCore attribute) {

        super( id, confirmationId, attribute );

        component = new Label( ACTION_ID,  new StringResourceModel( "webapp.common.confirmAttribute", this, null, new Object[] { attribute.getValue() } ));

        component.setOutputMarkupId( true );
        add( component );

    }
}

