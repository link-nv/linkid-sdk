/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.user.webapp.template;

import net.link.safeonline.webapp.template.TemplatePage;


/**
 * <h2>{@link UserTemplatePage}<br>
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
public abstract class UserTemplatePage extends TemplatePage {

    public static final String NAVIGATION_ID = "navigation_panel";


    public UserTemplatePage(NavigationPanel.Panel panel) {

        super();

        getHeader().add(new NavigationPanel(NAVIGATION_ID, panel));

    }

}
