/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.test;

import org.apache.wicket.Page;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.util.tester.ITestPageSource;


/**
 * <h2>{@link UrlPageSource}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class UrlPageSource implements ITestPageSource {

    private static final long serialVersionUID = 1L;
    private String            url;


    /**
     * TODO
     */
    public UrlPageSource(String url) {

        this.url = url;
    }

    /**
     * {@inheritDoc}
     */
    public Page getTestPage() {

        throw new RedirectToUrlException(url);
    }
}
