package net.link.safeonline.beid.webapp;


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

        return localize("beid");
    }
}
