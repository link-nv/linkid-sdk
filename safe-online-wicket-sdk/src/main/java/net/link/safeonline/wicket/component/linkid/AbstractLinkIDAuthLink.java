/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.component.linkid;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.link.util.wicket.util.ServletResponse;
import net.link.util.wicket.util.ServletResponseException;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.link.Link;


/**
 * <h2>{@link AbstractLinkIDAuthLink}<br> <sub>A link that uses the linkID SDK to log a user in through the linkID authentication
 * services.</sub></h2>
 * <p/>
 * <p> <i>Sep 22, 2008</i> </p>
 *
 * @author lhunath
 */
public abstract class AbstractLinkIDAuthLink extends Link<Object> implements LinkIDAuthDelegate {

    private final Class<? extends Page> target;
    private final PageParameters        targetParameters;

    public AbstractLinkIDAuthLink(String id) {

        this( id, null );
    }

    /**
     * @param target The {@link Page} to return to after the linkID delegation. <code>null</code>: Use the application's homepage.
     */
    public AbstractLinkIDAuthLink(String id, Class<? extends Page> target) {

        this( id, target, null );
    }

    /**
     * @param target The {@link Page} to return to after the linkID delegation. <code>null</code>: Use the application's homepage.
     */
    public AbstractLinkIDAuthLink(String id, Class<? extends Page> target, PageParameters targetParameters) {

        super( id );

        this.target = target;
        this.targetParameters = targetParameters;
    }

    @Override
    public void onClick() {

        throw new ServletResponseException( new ServletResponse() {
            public void respond(final HttpServletRequest request, final HttpServletResponse response)
                    throws IOException {

                delegate( request, response, getTarget(), getTargetPageParameters() );
            }
        } );
    }

    public Class<? extends Page> getTarget() {

        return target == null? Application.get().getHomePage(): target;
    }

    public PageParameters getTargetPageParameters() {

        return targetParameters;
    }
}
