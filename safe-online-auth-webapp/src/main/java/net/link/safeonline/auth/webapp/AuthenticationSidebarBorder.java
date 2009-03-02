package net.link.safeonline.auth.webapp;

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.webapp.template.SidebarBorder;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.image.Image;


public class AuthenticationSidebarBorder extends SidebarBorder {

    private static final long serialVersionUID = 1L;

    Log                       LOG              = LogFactory.getLog(getClass());


    public AuthenticationSidebarBorder(String id, String helpMessage, final boolean showHelpdeskLink) {

        super(id, helpMessage, showHelpdeskLink);

        Image logoImage = new Image("logo", "override");
        logoImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                + "/logo?applicationId=" + LoginManager.findApplication(WicketUtil.getHttpSession(getRequest()))));
        add(logoImage);
    }

}
