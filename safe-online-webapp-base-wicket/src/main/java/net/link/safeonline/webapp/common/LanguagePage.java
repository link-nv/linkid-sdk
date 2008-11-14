/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.common;

import java.util.Locale;

import javax.servlet.http.Cookie;

import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;


/**
 * <h2>{@link LanguagePage}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Nov 6, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class LanguagePage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    static final Log          LOG              = LogFactory.getLog(LanguagePage.class);


    public LanguagePage(final Page returnPage) {

        super();

        addHeader(this);

        // english link
        getContent().add(new Link<String>("en") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                setLanguage("en");
                setResponsePage(returnPage);

            }
        });

        // dutch link
        getContent().add(new Link<String>("nl") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                setLanguage("nl");
                setResponsePage(returnPage);

            }
        });

    }

    protected void setLanguage(String language) {

        Cookie languageCookie = new Cookie(SafeOnlineCookies.LANGUAGE_COOKIE, language);
        languageCookie.setPath("/");
        languageCookie.setMaxAge(60 * 60 * 24 * 30 * 6);
        getWebRequestCycle().getWebResponse().addCookie(languageCookie);

        getSession().setLocale(new Locale(language));
    }
}
