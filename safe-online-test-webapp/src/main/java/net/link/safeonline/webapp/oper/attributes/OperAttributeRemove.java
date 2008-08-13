/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper.attributes;

import net.link.safeonline.webapp.oper.OperTemplate;


public class OperAttributeRemove extends OperTemplate {

    public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX + "/attributes/attribute-remove.seam";


    public OperAttributeRemove() {

        super(PAGE_NAME);
    }

    public OperAttributes remove() {

        clickButtonAndWait("remove");
        return new OperAttributes();
    }

    public OperAttributes cancel() {

        clickButtonAndWait("cancel");
        return new OperAttributes();
    }
}
