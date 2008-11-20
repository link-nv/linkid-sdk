package net.link.safeonline.demo.wicket.web;

import org.apache.wicket.markup.html.WebPage;


public class OlasAuthRedirectPage extends WebPage {

    private static final long serialVersionUID = 1L;


    /**
     * Let the SDK handle the redirection to OLAS-Auth.
     */
    public OlasAuthRedirectPage() {

        new OlasLoginLink("olasLogin").onClick();
    }
}
