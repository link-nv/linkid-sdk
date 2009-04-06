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
import org.apache.wicket.util.tester.WicketTester;


/**
 * <h2>{@link UrlPageSource}<br>
 * <sub>A page source for the {@link WicketTester} to test mount points.</sub></h2>
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
