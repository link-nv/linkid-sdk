package net.link.safeonline.webapp.template;

import net.link.safeonline.webapp.common.LanguagePage;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.protocol.http.WebApplication;


public class HeaderBorder extends Border {

    private static final long serialVersionUID = 1L;


    public HeaderBorder(final String id, final TemplatePage page) {

        this(id, page, false);

    }

    public HeaderBorder(final String id, final TemplatePage page, final boolean logoutEnabled) {

        super(id);

        // theme.css
        String applicationName = WebApplication.get().getServletContext().getInitParameter("ApplicationName");

        String themeCSS = "theme.css?applicationName=" + applicationName;
        StyleSheetReference themeCSSRef = new StyleSheetReference("themeCSS", getClass(), themeCSS);
        themeCSSRef.add(new SimpleAttributeModifier("href", themeCSS));
        add(themeCSSRef);

        String pageTitle = WebApplication.get().getServletContext().getInitParameter("CommercialName");

        // page title
        add(new Label("pageTitle", pageTitle));

        // language link
        add(new Link<String>("language") {

            private static final long serialVersionUID = 1L;

            {
                Image languageImage = new Image("language_image", "override");
                languageImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest().getContextPath() + "/images/langs/"
                        + getLocale().getLanguage() + ".png"));
                add(languageImage);
            }


            @Override
            public void onClick() {

                throw new RestartResponseException(new LanguagePage(page));

            }
        });

        // logout link
        add(new OlasLogoutLink("logout", getApplication().getHomePage()).setVisible(logoutEnabled));
    }
}
