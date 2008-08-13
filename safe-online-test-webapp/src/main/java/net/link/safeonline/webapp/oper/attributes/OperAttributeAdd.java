/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.webapp.oper.attributes;

import net.link.safeonline.webapp.oper.OperTemplate;


public class OperAttributeAdd extends OperTemplate {

    public static final String PAGE_NAME = SAFE_ONLINE_OPER_WEBAPP_PREFIX + "/attributes/attribute-add.seam";


    public OperAttributeAdd() {

        super(PAGE_NAME);
    }

    public void setName(String name) {

        fillInputField("name", name);
    }

    public void setSingleValued() {

        clickRadioButton("singleValued");
    }

    public void setMultiValued() {

        clickRadioButton("multiValued");
    }

    public void setCompounded() {

        clickRadioButton("compounded");
    }

    public void setDeviceAttribute(boolean value) {

        setCheckBox("deviceAttribute", value);
    }

    public OperAttributes cancel() {

        clickButtonAndWait("cancel");
        return new OperAttributes();
    }

    public void next() {

        clickButtonAndWait("next");
    }

}
