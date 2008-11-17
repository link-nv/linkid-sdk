package net.link.safeonline.webapp.template;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;


public class ProgressRegistrationPanel extends Panel {

    private static final long serialVersionUID = 1L;


    public enum stage {
        choose,
        initial,
        register
    }


    public ProgressRegistrationPanel(String id, stage stage) {

        super(id);

        String setActiveScript = "function setActive(id, className)" + "{ obj = document.getElementById(id); " + "if (obj != null) "
                + "{ obj.className = className; }" + " }" + " setActive(\"stage_" + stage + "\", \"active\");";

        add(new Label("setActive", setActiveScript).setEscapeModelStrings(false));
    }
}
