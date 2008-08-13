/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper.attributes;

import net.link.safeonline.webapp.oper.OperTemplate;


public class OperAttributes extends OperTemplate {

    public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX + "/attributes/attributes.seam";


    public OperAttributes() {

        super(PAGE_NAME);
    }

    public OperAttributeAdd add() {

        clickButtonAndWait("add");
        return new OperAttributeAdd();
    }

    public boolean isAttributePresent(String name) {

        return checkRowLink("attributes-data", name);

    }

    public OperAttributeRemove removeAttribute(String name) {

        clickLinkInRowLinkAndWait("attributes-data", name, "remove");
        return new OperAttributeRemove();
    }
}
