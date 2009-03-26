/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import org.apache.wicket.behavior.AbstractHeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;


/**
 * <h2>{@link FocusOnReady}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Jan 21, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class FocusOnReady extends AbstractHeaderContributor {

    private static final long  serialVersionUID = 1L;

    private IHeaderContributor headercontributer;


    public FocusOnReady(String path) {

        setPath(path);
    }

    public void setPath(final String path) {

        headercontributer = new IHeaderContributor() {

            private static final long serialVersionUID = 1L;


            public void renderHead(IHeaderResponse response) {

                response.renderOnDomReadyJavascript(String.format("document.getElementById('%s').focus()", path.replaceAll("'", "\\'")));
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IHeaderContributor[] getHeaderContributors() {

        return new IHeaderContributor[] { headercontributer };
    }
}
