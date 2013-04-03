/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import net.link.safeonline.sdk.api.auth.LoginMode;
import net.link.safeonline.sdk.api.auth.StartPage;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;


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

    protected LoginMode loginMode;
    protected StartPage startPage;

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
    public boolean isVisible() {

        return !LinkIDWicketUtils.isLinkIDAuthenticated();
    }
}
