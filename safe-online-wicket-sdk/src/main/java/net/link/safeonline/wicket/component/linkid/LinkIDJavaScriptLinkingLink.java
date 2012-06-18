/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.safeonline.sdk.api.auth.LinkMode;
import net.link.safeonline.wicket.util.LinkIDWicketUtils;
import org.apache.wicket.*;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link LinkIDJavaScriptLinkingLink}<br> <sub>A link that uses the linkID SDK to log a user in through the linkID authentication
 * services.
 * Needs 'linkid.js' on the page, but this class can add it itself.
 * <p/>
 * </sub></h2>
 * <p/>
 * <p> <i>Nov 24, 2011</i> </p>
 *
 * @author sgdesmet
 */
public class LinkIDJavaScriptLinkingLink extends AbstractLinkIDAuthJSLink {

    private String mobileLinkURL = "/mobile_link";

    /**
     * Constructor. Adds 'linkid.js' to the page.
     */
    public LinkIDJavaScriptLinkingLink(String id) {

        this( id, null, true );
    }

    /**
     * Constructor. Adds 'linkid.js' to the page.
     */
    public LinkIDJavaScriptLinkingLink(String id, Class<? extends Page> target) {

        this( id, target, true );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    public LinkIDJavaScriptLinkingLink(String id, boolean addJS) {

        this( id, null, addJS );
    }

    /**
     * Constructor. If addJS is true, the 'linkid.js' javascript will be added automatically to the page.
     * If false, it is the task of the web developer to ensure that this JavaScript is added. This component will not work
     * without it.
     */
    public LinkIDJavaScriptLinkingLink(String id, @Nullable Class<? extends Page> target, boolean addJS) {

        super( id, "link", target, addJS );
    }

    public String getMobileLinkURL() {

        return mobileLinkURL;
    }

    public void setMobileLinkURL(final String mobileLinkURL) {

        this.mobileLinkURL = mobileLinkURL;
    }

    @Override
    public void delegate(final HttpServletRequest request, final HttpServletResponse response, final Class<? extends Page> target,
                         final PageParameters targetPageParameters) {

        throw new RedirectToUrlException( mobileLinkURL );
    }

    @Override
    public boolean isVisible() {

        return LinkIDWicketUtils.isLinkIDAuthenticated();
    }

    @Nullable
    public LinkMode findLinkMode() {

        return null;
    }
}
