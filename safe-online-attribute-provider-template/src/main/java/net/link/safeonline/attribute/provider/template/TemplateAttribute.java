package net.link.safeonline.attribute.provider.template;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.AttributeType;


public interface TemplateAttribute extends Serializable {

    AttributeType getAttributeType();

    List<AttributeCore> listAttributes(int size);

    AttributeCore findAttribute();
}
