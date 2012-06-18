/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.jetbrains.annotations.Nullable;


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
public abstract class AbstractLinkIDAuthJSLink extends AbstractLinkIDAuthLink {

    protected boolean addJS;

    /**
     * Constructor. Adds 'linkid.login.js' to the page.
     */
    protected AbstractLinkIDAuthJSLink(String id, String linkClass) {

        this( id, linkClass, null, true );
    }

    /**
     * Constructor. Adds 'linkid.login.js' to the page.
     */
    protected AbstractLinkIDAuthJSLink(String id, String linkClass, Class<? extends Page> target) {

        this( id, linkClass, target, true );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.login.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    protected AbstractLinkIDAuthJSLink(String id, String linkClass, boolean addJS) {

        this( id, linkClass, null, addJS );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.login.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    protected AbstractLinkIDAuthJSLink(String id, String linkClass, @Nullable Class<? extends Page> target, boolean addJS) {

        super( id, target );
        this.addJS = addJS;
        add( new AttributeAppender( "class", new Model<String>( String.format( "linkid-%s", linkClass ) ), " " ) );
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

                    response.renderJavascriptReference( String.format( "%s/js/linkid-min.js", config().web().staticBase() ),
                            "linkid-login-script" );
                }
            } ) );
        }
    }
}
