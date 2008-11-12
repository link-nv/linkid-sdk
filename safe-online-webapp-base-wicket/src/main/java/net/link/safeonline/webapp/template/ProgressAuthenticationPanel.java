package net.link.safeonline.webapp.template;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


public class ProgressAuthenticationPanel extends Panel {

    private static final long serialVersionUID = 1L;


    public enum stage {
        authenticate,
        agreements,
        attributes,
        select
    }


    public ProgressAuthenticationPanel(String id, stage stage) {

        super(id);

        String setActiveScript = "function setActive(id, className)" + "{ obj = document.getElementById(id); " + "if (obj != null) "
                + "{ obj.className = className; }" + " }" + " setActive(\"stage_" + stage + "\", \"active\");";

        add(new Label("setActive", setActiveScript).setEscapeModelStrings(false));
    }
}
