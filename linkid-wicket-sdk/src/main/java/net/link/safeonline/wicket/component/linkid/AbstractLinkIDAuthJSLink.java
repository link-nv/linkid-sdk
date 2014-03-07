/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.wicket.component.linkid;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link AbstractLinkIDAuthJSLink}<br> <sub>A link that uses the linkID SDK to log a user in through the linkID authentication
 * services.
 * Needs 'linkid.js' on the page, but this class can add it itself.
 * <p/>
 * </sub></h2>
 * <p/>
 * <p> <i>Nov 24, 2011</i> </p>
 *
 * @author sgdesmet
 */
public abstract class AbstractLinkIDAuthJSLink extends WebMarkupContainer {

    protected boolean addJS;

    /**
     * Constructor. Adds 'linkid-min.js' to the page.
     */
    protected AbstractLinkIDAuthJSLink(String id) {

        this( id, true );
    }

    /**
     * Constructor. If addJS is true, the 'linkid-min.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    protected AbstractLinkIDAuthJSLink(String id, boolean addJS) {

        super( id );
        this.addJS = addJS;
        add( new AttributeAppender( "class", new Model<String>( "linkid-login" ), " " ) );
    }

    public boolean isAddJS() {

        return addJS;
    }

    public void setAddJS(final boolean addJS) {

        this.addJS = addJS;
    }

    @Override
    protected void onBeforeRender() {

        super.onBeforeRender();
        if (addJS) {
            //LinkID JavaScript which handles login look
            add( new HeaderContributor( new IHeaderContributor() {
                @Override
                public void renderHead(IHeaderResponse response) {

                    response.renderJavascriptReference( String.format( "%s/js/linkid-min.js", config().web().staticBase() ), "linkid-login-script" );
                }
            } ) );
        }
    }
}
