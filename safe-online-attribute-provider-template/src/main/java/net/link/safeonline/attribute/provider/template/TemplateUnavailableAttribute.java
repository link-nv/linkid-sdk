package net.link.safeonline.attribute.provider.template;

import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.AttributeType;
import net.link.safeonline.attribute.provider.DataType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TemplateUnavailableAttribute implements TemplateAttribute {

    private static final Log LOG = LogFactory.getLog( TemplateUnavailableAttribute.class );

    private final String providerJndi;

    public TemplateUnavailableAttribute(final String providerJndi) {
        this.providerJndi = providerJndi;
    }

    public AttributeType getAttributeType() {
        return new AttributeType( "template.unavailable", DataType.STRING, providerJndi, true, false, true, false, false );
    }

    public List<AttributeCore> listAttributes(int size) {

        List<AttributeCore> attributes = new LinkedList<AttributeCore>();
        for (int i = 0; i < size; i++) {
            AttributeCore attribute = new AttributeCore( Integer.toString( i ), getAttributeType() );
            attribute.setUnavailable( true );
            attributes.add( attribute );
        }
        return attributes;
    }

    public AttributeCore findAttribute() {

        AttributeCore attribute = new AttributeCore( "0", getAttributeType() );
        attribute.setUnavailable( true );
        return attribute;
    }
}
