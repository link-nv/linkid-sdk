package net.link.safeonline.webapp.template;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;


public class ContentBorder extends Border {

    private static final long serialVersionUID = 1L;


    public ContentBorder(final String id, String pageTitle) {

        super(id);

        add(new Label("pageTitle", pageTitle));
    }
}
