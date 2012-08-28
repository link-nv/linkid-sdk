/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket;

import net.link.util.wicket.component.WicketPage;
import org.apache.wicket.*;
import org.apache.wicket.protocol.http.WebSession;
import org.jetbrains.annotations.Nullable;


/**
 * <h2>{@link LinkIDSession}<br>
 * <sub>An abstract wicket session which allows the wicket tools backend to check whether the user was authenticated.</sub></h2>
 *
 * <p>
 * Currently, you should use this {@link LinkIDSession} instead of wicket's {@link Session} so that the {@link WicketPage} can properly
 * identify whether the user is authenticated before rendering a page that requires him to be.
 * </p>
 *
 * <p>
 * <i>Dec 31, 2008</i>
 * </p>
 *
 * @author lhunath
 */
public abstract class LinkIDSession extends WebSession {

    private Class<? extends Page> postAuthenticationPage;

    private PageParameters postAuhtenticationParameters;

    // USER ---------------------------------------------------------

    /**
     * @return The linkID application-specific user identifier as known by the application's specific user representation. Should be
     *         {@code null} if the application thinks no user is logged in yet.
     */
    @Nullable
    public abstract String findUserLinkID();

    /**
     * If this yields {@code false}, LinkIDApplicationPage#onLinkIDAuthenticated() and LinkIDApplicationPage#postAuth()
     * need be processed, still.
     *
     * @return {@code true} if this application's application-specific user is known to it.
     */
    public abstract boolean isUserSet();

    /**
     * Invoked when a linkID user has logged into your application that is <b>different from {@link #findUserLinkID()}</b>, OR when
     * {@link #findUserLinkID()} indicates a user is logged in, but there is no linkID user currently logged in.
     *
     * @return {@code true} Application could not log out the user cleanly. The wicket HTTP session will be invalidated instead.
     */
    public abstract boolean logout();

    // POST AUTHENTICATION REDIRECTION ------------------------------

    public Class<? extends Page> getPostAuthenticationPage() {

        return postAuthenticationPage;
    }

    public void setPostAuthenticationPage(@Nullable Class<? extends Page> postAuthenticationPage) {

        this.postAuthenticationPage = postAuthenticationPage;
    }

    public PageParameters getPostAuhtenticationParameters() {

        return postAuhtenticationParameters;
    }

    public void setPostAuhtenticationParameters(@Nullable final PageParameters postAuhtenticationParameters) {

        this.postAuhtenticationParameters = postAuhtenticationParameters;
    }

    // GLOBAL -------------------------------------------------------

    public static LinkIDSession get() {

        return (LinkIDSession) Session.get();
    }

    public LinkIDSession(Request request) {

        super( request );
    }
}
