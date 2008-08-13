/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import net.link.safeonline.data.AttributeDO;


/**
 * JSF output component for name of {@link AttributeDO}. The visual effect will depend on the category of the attribute.
 * A compounded entry will appear in bold, a member entry will appear in italic.
 *
 * @author fcorneli
 *
 */
public class AttributeNameOutputComponent extends UIOutput {

    public static final String ATTRIBUTE_NAME_OUTPUT_COMPONENT_TYPE = "net.link.component.attributeNameOutput";

    private String             compoundedStyleClass                 = "";

    public final static String COMPOUNDED_DEFAULT                   = "so-nameoutput-compounded";

    private String             memberStyleClass                     = "";

    public final static String MEMBER_DEFAULT                       = "so-nameoutput-member";

    private String             styleClass                           = "";

    public final static String STYLE_DEFAULT                        = "so-nameoutput";

    private boolean            optional                             = false;


    @Override
    public void encodeBegin(FacesContext context) throws IOException {

        ResponseWriter response = context.getResponseWriter();
        response.startElement("span", this);
        String clientId = getClientId(context);
        response.writeAttribute("id", clientId, "id");
        response.writeAttribute("class", STYLE_DEFAULT + " " + this.styleClass, "styleClass");
        AttributeDO attribute = (AttributeDO) getValue();
        String attributeName = attribute.getHumanReadableName();
        ResourceBundle messages = TaglibUtil.getResourceBundle(context);
        String optionalStr = messages.getString("optional");
        if (this.optional) {
            attributeName += " ( " + optionalStr + " )";
        }
        if (attribute.isCompounded()) {
            response.startElement("span", null);
            response.writeAttribute("class", COMPOUNDED_DEFAULT + " " + this.compoundedStyleClass,
                    "compoundedStyleClass");
            response.write(attributeName);
            response.endElement("span");
        } else if (attribute.isMember()) {
            response.startElement("span", null);
            response.writeAttribute("class", MEMBER_DEFAULT + " " + this.memberStyleClass, "memberStyleClass");
            response.write(attributeName);
            response.endElement("span");
        } else {
            response.write(attributeName);
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {

        ResponseWriter response = context.getResponseWriter();
        response.endElement("span");
    }

    public String getCompoundedStyleClass() {

        return this.compoundedStyleClass;
    }

    public void setCompoundedStyleClass(String compoundedStyleClass) {

        this.compoundedStyleClass = compoundedStyleClass;
    }

    public String getMemberStyleClass() {

        return this.memberStyleClass;
    }

    public void setMemberStyleClass(String memberStyleClass) {

        this.memberStyleClass = memberStyleClass;
    }

    public String getStyleClass() {

        return this.styleClass;
    }

    public void setStyleClass(String styleClass) {

        this.styleClass = styleClass;
    }

    public boolean getOptional() {

        return this.optional;
    }

    public void setOptional(boolean optional) {

        this.optional = optional;
    }
}
