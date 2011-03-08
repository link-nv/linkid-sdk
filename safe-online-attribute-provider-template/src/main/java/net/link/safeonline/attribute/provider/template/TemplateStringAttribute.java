package net.link.safeonline.attribute.provider.template;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.AttributeType;
import net.link.safeonline.attribute.provider.DataType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TemplateStringAttribute implements TemplateAttribute {

    private static final Log LOG = LogFactory.getLog( TemplateStringAttribute.class );

    private final String providerJndi;

    public TemplateStringAttribute(final String providerJndi) {
        this.providerJndi = providerJndi;
    }

    public AttributeType getAttributeType() {
        return new AttributeType( "template.string", DataType.STRING, providerJndi, true, false, true, false, true );
    }

    public List<AttributeCore> listAttributes(int size) {

        List<AttributeCore> attributes = new LinkedList<AttributeCore>();
        for (int i = 0; i < size; i++) {
            attributes.add( new AttributeCore( Integer.toString( i ), getAttributeType(), "string-" + UUID.randomUUID() ) );
        }
        return attributes;
    }

    public AttributeCore findAttribute() {

        return new AttributeCore( "0", getAttributeType(), "string-" + UUID.randomUUID() );
    }
}
