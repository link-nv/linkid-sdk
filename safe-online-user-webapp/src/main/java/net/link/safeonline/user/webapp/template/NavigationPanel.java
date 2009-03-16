/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.webapp.template;

import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.wicket.web.OLASSession;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.panel.Panel;


/**
 * <h2>{@link NavigationPanel}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 16, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class NavigationPanel extends Panel {

    private static final long  serialVersionUID     = 1L;

    public static final String HOME_LINK_ID         = "home_link";
    public static final String PROFILE_LINK_ID      = "profile_link";
    public static final String APPLICATIONS_LINK_ID = "applications_link";
    public static final String DEVICES_LINK_ID      = "devices_link";
    public static final String ACCOUNT_LINK_ID      = "account_link";

    public static final String SET_CLASS_SCRIPT_ID  = "setClass";


    public enum Panel {
        home,
        profile,
        applications,
        devices,
        account
    }


    public NavigationPanel(String id, Panel panel) {

        super(id);

        add(new PageLink<String>(HOME_LINK_ID, MainPage.class));
        add(new PageLink<String>(PROFILE_LINK_ID, MainPage.class));
        add(new PageLink<String>(APPLICATIONS_LINK_ID, MainPage.class));
        add(new PageLink<String>(DEVICES_LINK_ID, MainPage.class));
        add(new PageLink<String>(ACCOUNT_LINK_ID, MainPage.class));

        String setClassScript = "function setClass(id, className)" + "{ obj = document.getElementById(id); " + "if (obj != null) "
                + "{ obj.className = className; }" + " }" + " setClass(\"page_" + panel + "\", \"selected\");";

        add(new Label(SET_CLASS_SCRIPT_ID, setClassScript).setEscapeModelStrings(false));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVisible() {

        return OLASSession.get().isUserSet();
    }
}
