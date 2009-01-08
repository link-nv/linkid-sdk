package net.link.safeonline.encap.webapp;

import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PageLink;


public class MainPage extends TemplatePage {

    private static final long   serialVersionUID    = 1L;

    public static final String  REGISTER_ID         = "register";
    public static final String  REMOVE_ID           = "remove";
    private static final String REMOVE_SERVLET_PATH = "remove";


    public MainPage() {

        getHeader();
        getContent().add(new PageLink(REGISTER_ID, RegistrationPage.class));
        getContent().add(new ExternalLink(REMOVE_ID, REMOVE_SERVLET_PATH));
    }
}
