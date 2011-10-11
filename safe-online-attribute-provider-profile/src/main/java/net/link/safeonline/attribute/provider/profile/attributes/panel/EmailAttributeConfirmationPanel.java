/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.attribute.provider.profile.attributes.panel;

import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.Compound;
import net.link.safeonline.attribute.provider.confirmation.AttributeConfirmationPanel;
import net.link.safeonline.attribute.provider.profile.attributes.EmailAddressAttribute;
import net.link.safeonline.attribute.provider.profile.attributes.EmailAttribute;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;


public class EmailAttributeConfirmationPanel extends AttributeConfirmationPanel {

    private static final Log LOG = LogFactory.getLog( EmailAttributeConfirmationPanel.class );

    public static final String ACTION_ID = "action";

    Label component;

    public EmailAttributeConfirmationPanel(String id, String confirmationId, final AttributeCore attribute) {

        super( id, confirmationId, attribute );

        AttributeCore emailAddressAttribute = (AttributeCore)((Compound)attribute.getValue()).findMember( EmailAddressAttribute.NAME );

        if (emailAddressAttribute != null && emailAddressAttribute.getValue() != null && !emailAddressAttribute.getValue().equals( "" ))
            component = new Label( ACTION_ID,  new StringResourceModel( "webapp.common.confirmAttribute", this, null, new Object[] { (String)emailAddressAttribute.getValue() } ));
        else
            component = new Label( ACTION_ID,  new StringResourceModel( "webapp.common.profile.email.emailExpired", this, null ));

        component.setOutputMarkupId( true );
        add( component );

    }
}

