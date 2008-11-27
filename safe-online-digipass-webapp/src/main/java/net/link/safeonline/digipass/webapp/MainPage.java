package net.link.safeonline.digipass.webapp;

import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.link.PageLink;


public class MainPage extends TemplatePage {

    private static final long  serialVersionUID = 1L;

    public static final String REGISTER_ID      = "register";
    public static final String REMOVE_ID        = "remove";


    public MainPage() {

        super();

        addHeader(this);

        getContent().add(new PageLink("register", RegisterPage.class));
        getContent().add(new PageLink("remove", RemovePage.class));

    }
}
