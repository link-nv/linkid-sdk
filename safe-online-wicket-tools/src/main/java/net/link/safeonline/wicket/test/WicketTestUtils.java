/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.test;

import java.lang.reflect.Field;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.servlet.LogoutServlet;
import net.link.safeonline.sdk.test.DummyNameIdentifierMappingClient;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.OlasAuthDelegate;
import net.link.safeonline.wicket.web.OlasAuthRedirectPage;
import net.link.safeonline.wicket.web.OlasLoginLink;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.wicket.AbortException;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.MockHttpServletResponse;


/**
 * <h2>{@link WicketTestUtils}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Apr 3, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class WicketTestUtils {

    /**
     * Uses {@link DummyNameIdentifierMappingClient} to find the userId that will be set as authenticated, and uses <code>test-device</code>
     * as the name of the device used for the authentication.
     * 
     * @see #mockLoginLink(MarkupContainer, String, String)
     */
    public static void mockLoginLink(MarkupContainer container)
            throws Exception {

        WicketTestUtils.mockLoginLink(container, DummyNameIdentifierMappingClient.getUserId(), "test-device");
    }

    /**
     * Replaces the delegate of all {@link OlasLoginLink} children of the given container by a delegate that set the given userId as
     * authenticated with the given authenticatedDevice as soon as the link is clicked.
     */
    public static void mockLoginLink(final MarkupContainer container, final String userId, final String authenticatedDevice)
            throws Exception {

        final OlasAuthDelegate delegateMock = new OlasAuthDelegate() {

            private static final long serialVersionUID = 1L;


            public void delegate(Class<? extends Page> target, HttpServletRequest request, HttpServletResponse response) {

                LoginManager.setUserId(userId, request);
                LoginManager.setAuthenticatedDevice(authenticatedDevice, request);

                throw new RestartResponseException(target);
            }
        };

        injectLoginDelegate(container, delegateMock);
    }

    private static void injectLoginDelegate(final MarkupContainer container, final OlasAuthDelegate delegateMock)
            throws Exception {

        Iterator<? extends Component> it = container.iterator();
        while (it.hasNext()) {
            Component child = it.next();

            // A normal link that sends us to OLAS: change the delegate of the OlasLoginLink to the mock delegate.
            if (child instanceof OlasLoginLink) {
                for (Class<?> c = child.getClass(); c != Object.class; c = c.getSuperclass()) {
                    try {
                        Field delegateField = c.getDeclaredField("delegate");

                        delegateField.setAccessible(true);
                        delegateField.set(child, delegateMock);
                    } catch (NoSuchFieldException e) {
                    }
                }
            }

            // An iframe that contains a redirection page which sends us to OLAS: run the mock delegate manually.
            else if (child instanceof InlineFrame) {
                for (Class<?> c = child.getClass(); c != Object.class; c = c.getSuperclass()) {
                    try {
                        Field pageLinkField = child.getClass().getDeclaredField("pageLink");
                        pageLinkField.setAccessible(true);
                        IPageLink pageLink = (IPageLink) pageLinkField.get(child);

                        if (OlasAuthRedirectPage.class.isAssignableFrom(pageLink.getPageIdentity())) {
                            try {
                                delegateMock.delegate(Application.get().getHomePage(), //
                                        WicketUtil.getServletRequest(), WicketUtil.getServletResponse());
                            } catch (AbortException e) {
                            }
                        }
                    } catch (NoSuchFieldException e) {
                    }
                }
            }

            // Recursively iterate children.
            if (child instanceof MarkupContainer) {
                injectLoginDelegate((MarkupContainer) child, delegateMock);
            }
        }
    }

    /**
     * Replaces the delegate of all {@link OlasLogoutLink} children of the given container by a delegate that set the given userId as
     * authenticated with the given authenticatedDevice as soon as the link is clicked.
     * 
     * @param logoutServlet
     *            The servlet that handles the application's logout procedure. If <code>null</code>, the session is invalidated on logout.
     */
    public static void mockLogoutLink(final MarkupContainer container, final Class<? extends HttpServlet> logoutServlet)
            throws Exception {

        OlasAuthDelegate delegateMock = new OlasAuthDelegate() {

            private static final long serialVersionUID = 1L;


            public void delegate(Class<? extends Page> target, HttpServletRequest request, HttpServletResponse response) {

                try {
                    if (logoutServlet == null) {
                        LoginManager.invalidateSession(request);
                    } else {
                        // LogoutServlets respond to GET.
                        ((MockHttpServletRequest) request).setMethod("GET");

                        logoutServlet.newInstance().service(request, response);

                        // Undo LogoutServlet redirects: We do our own.
                        ((MockHttpServletResponse) response).initialize();
                    }

                    throw new RestartResponseException(target);
                }

                catch (AbortException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                finally {
                    if (request.getSession().getAttribute(LogoutServlet.INVALIDATE_SESSION) != null) {
                        Session.get().invalidateNow();
                    }
                }
            }
        };

        injectLogoutDelegate(container, delegateMock);
    }

    private static void injectLogoutDelegate(MarkupContainer container, OlasAuthDelegate delegateMock)
            throws NoSuchFieldException, IllegalAccessException {

        Iterator<? extends Component> it = container.iterator();
        while (it.hasNext()) {
            Component child = it.next();

            if (child instanceof OlasLogoutLink) {
                for (Class<?> c = child.getClass(); c != Object.class; c = c.getSuperclass()) {
                    try {
                        Field delegateField = c.getDeclaredField("delegate");

                        delegateField.setAccessible(true);
                        delegateField.set(child, delegateMock);
                    } catch (NoSuchFieldException e) {
                    }
                }
            }

            // Recursively iterate children.
            if (child instanceof MarkupContainer) {
                injectLogoutDelegate((MarkupContainer) child, delegateMock);
            }
        }
    }
}
