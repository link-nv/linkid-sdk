/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import net.link.safeonline.sdk.api.auth.StartPage;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link LinkIDJavaScriptLoginLink}<br> <sub>A link that uses the linkID SDK to log a user in through the linkID authentication
 * services.
 * Needs 'linkid.login.js' on the page, but this class can add it itself.
 * <p/>
 * </sub></h2>
 * <p/>
 * <p> <i>Nov 24, 2011</i> </p>
 *
 * @author sgdesmet
 */
@SuppressWarnings("UnusedDeclaration")
public class LinkIDJavaScriptLoginLink extends AbstractLinkIDAuthJSLink {

    boolean   mobileAuthentication;
    boolean   mobileAuthenticationMinimal;
    String    targetURI;
    StartPage startPage;
    String    initLoginPath;

    /**
     * Constructor. Adds 'linkid-min.js' to the page.
     */
    public LinkIDJavaScriptLoginLink(String id) {

        super( id, true );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.login.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    public LinkIDJavaScriptLoginLink(String id, boolean addJS) {

        super( id, addJS );
    }

    @Override
    protected void onBeforeRender() {

        super.onBeforeRender();

        configureLoginLink();
    }

    private void configureLoginLink() {

        if (mobileAuthentication)
            add( new AttributeModifier( "data-mobile", true, new Model<String>( "true" ) ) );
        if (mobileAuthenticationMinimal)
            add( new AttributeModifier( "data-mobile-minimal", true, new Model<String>( "true" ) ) );
        if (null != targetURI)
            add( new AttributeModifier( "data-completion-href", true, new Model<String>( targetURI ) ) );
        if (null != startPage)
            add( new AttributeModifier( "data-start-page", true, new Model<String>( startPage.name() ) ) );
        if (null != initLoginPath)
            add( new AttributeModifier( "data-login-href", true, new Model<String>( initLoginPath ) ) );
    }

    @Override
    public boolean isVisible() {

        return !LinkIDWicketUtils.isLinkIDAuthenticated();
    }

    public boolean isMobileAuthentication() {

        return mobileAuthentication;
    }

    public void setMobileAuthentication(final boolean mobileAuthentication) {

        this.mobileAuthentication = mobileAuthentication;
    }

    public boolean isMobileAuthenticationMinimal() {

        return mobileAuthenticationMinimal;
    }

    public void setMobileAuthenticationMinimal(final boolean mobileAuthenticationMinimal) {

        this.mobileAuthenticationMinimal = mobileAuthenticationMinimal;
    }

    public String getTargetURI() {

        return targetURI;
    }

    public void setTargetURI(final String targetURI) {

        this.targetURI = targetURI;
    }

    public StartPage getStartPage() {

        return startPage;
    }

    public void setStartPage(final StartPage startPage) {

        this.startPage = startPage;
    }

    public String getInitLoginPath() {

        return initLoginPath;
    }

    public void setInitLoginPath(final String initLoginPath) {

        this.initLoginPath = initLoginPath;
    }
}
