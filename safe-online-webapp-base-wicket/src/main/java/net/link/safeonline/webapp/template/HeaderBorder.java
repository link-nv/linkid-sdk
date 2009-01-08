package net.link.safeonline.webapp.template;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.webapp.common.LanguagePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.protocol.http.WebApplication;


public class HeaderBorder extends Border {

    private static final long serialVersionUID = 1L;


    public HeaderBorder(final String id, final Page page) {

        this(id, page, false);

    }

    public HeaderBorder(final String id, final Page page, final boolean logoutEnabled) {

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
                languageImage.add(new SimpleAttributeModifier("src", WicketUtil.toServletRequest(getRequest()).getContextPath()
                        + "/images/langs/" + getLocale().getLanguage() + ".png"));
                add(languageImage);
            }


            @Override
            public void onClick() {

                setResponsePage(new LanguagePage(page));

            }
        });

        // logout link
        add(new Link<String>("logout") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                // TODO Auto-generated method stub
            }

            @Override
            public boolean isVisible() {

                return logoutEnabled && null != LoginManager.findUserId(WicketUtil.toServletRequest(getRequest()));
            }
        });
    }
}
