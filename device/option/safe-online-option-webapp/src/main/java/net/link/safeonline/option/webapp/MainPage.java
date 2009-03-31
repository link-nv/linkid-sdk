package net.link.safeonline.option.webapp;

import net.link.safeonline.webapp.template.TemplatePage;


public class MainPage extends TemplatePage {

    private static final long serialVersionUID = 1L;


    public MainPage() {

        getHeader();
        getContent();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("option");
    }
}
