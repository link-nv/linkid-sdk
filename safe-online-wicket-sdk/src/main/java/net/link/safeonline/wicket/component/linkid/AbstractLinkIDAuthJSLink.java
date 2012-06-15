/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import static net.link.safeonline.sdk.configuration.SDKConfigHolder.*;

import net.link.safeonline.sdk.api.auth.LoginMode;
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
 * Needs 'linkid.login.js' on the page, but this class can add it itself.
 * <p/>
 * </sub></h2>
 * <p/>
 * <p> <i>Nov 24, 2011</i> </p>
 *
 * @author sgdesmet
 */
public abstract class AbstractLinkIDAuthJSLink extends AbstractLinkIDAuthLink {

    protected boolean addJS;
    protected LoginMode loginMode = null;

    /**
     * Constructor. Adds 'linkid.login.js' to the page.
     */
    public AbstractLinkIDAuthJSLink(String id, String linkClass) {

        this( id, linkClass, null, true );
    }

    /**
     * Constructor. Adds 'linkid.login.js' to the page.
     */
    public AbstractLinkIDAuthJSLink(String id, String linkClass, Class<? extends Page> target) {

        this( id, linkClass, target, true );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.login.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    public AbstractLinkIDAuthJSLink(String id, String linkClass, boolean addJS) {

        this( id, linkClass, null, addJS );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.login.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    public AbstractLinkIDAuthJSLink(String id, String linkClass, @Nullable Class<? extends Page> target, boolean addJS) {

        super( id, target );
        this.addJS = addJS;
        add( new AttributeAppender( "class", new Model<String>( "linkid-" + linkClass ), " " ) );
    }

    public LoginMode getLoginMode() {

        return loginMode;
    }

    /**
     * Set the login style (redirect, popup window, modal window). Only used if this parameter has not already been set by linkid.login.js
     */
    public void setLoginMode(final LoginMode loginMode) {

        this.loginMode = loginMode;
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
        if (loginMode != null) {
            add( new AttributeAppender( "data-mode", new Model<String>( loginMode.toString().toLowerCase() ), " " ) );
        }
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
