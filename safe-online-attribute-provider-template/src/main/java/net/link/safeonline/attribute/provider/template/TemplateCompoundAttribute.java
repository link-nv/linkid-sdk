package net.link.safeonline.attribute.provider.template;

import java.util.LinkedList;
import java.util.List;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.AttributeType;
import net.link.safeonline.attribute.provider.Compound;
import net.link.safeonline.attribute.provider.DataType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TemplateCompoundAttribute implements TemplateAttribute {

    private static final Log LOG = LogFactory.getLog( TemplateCompoundAttribute.class );

    private final String providerJndi;

    public TemplateCompoundAttribute(final String providerJndi) {
        this.providerJndi = providerJndi;
    }

    public AttributeType getAttributeType() {
        AttributeType compoundAttributeType = new AttributeType( "template.compound", DataType.COMPOUNDED, providerJndi, true, false, true,
                                                                  false, true );

        compoundAttributeType.getMembers().add( new TemplateStringAttribute( providerJndi ).getAttributeType() );
        compoundAttributeType.getMembers().add( new TemplateBooleanAttribute( providerJndi ).getAttributeType() );
        compoundAttributeType.getMembers().add( new TemplateDateAttribute( providerJndi ).getAttributeType() );
        compoundAttributeType.getMembers().add( new TemplateDoubleAttribute( providerJndi ).getAttributeType() );
        compoundAttributeType.getMembers().add( new TemplateIntegerAttribute( providerJndi ).getAttributeType() );

        return compoundAttributeType;
    }

    public List<AttributeCore> listAttributes(int size) {

        List<AttributeCore> attributes = new LinkedList<AttributeCore>();

        List<AttributeCore> stringAttributes = new TemplateStringAttribute( providerJndi ).listAttributes( size );
        List<AttributeCore> booleanAttributes = new TemplateBooleanAttribute( providerJndi ).listAttributes( size );
        List<AttributeCore> dateAttributes = new TemplateDateAttribute( providerJndi ).listAttributes( size );
        List<AttributeCore> doubleAttributes = new TemplateDoubleAttribute( providerJndi ).listAttributes( size );
        List<AttributeCore> integerAttributes = new TemplateIntegerAttribute( providerJndi ).listAttributes( size );

        for (int i = 0; i < size; i++) {

            List<AttributeCore> members = new LinkedList<AttributeCore>();
            members.add( stringAttributes.get( i ) );
            members.add( booleanAttributes.get( i ) );
            members.add( dateAttributes.get( i ) );
            members.add( doubleAttributes.get( i ) );
            members.add( integerAttributes.get( i ) );
            attributes.add( new AttributeCore( Integer.toString( i ), getAttributeType(), new Compound( members ) ) );
        }
        return attributes;
    }

    public AttributeCore findAttribute() {

        List<AttributeCore> members = new LinkedList<AttributeCore>();
        members.add( new TemplateStringAttribute( providerJndi ).findAttribute() );
        members.add( new TemplateBooleanAttribute( providerJndi ).findAttribute() );
        members.add( new TemplateDateAttribute( providerJndi ).findAttribute() );
        members.add( new TemplateDoubleAttribute( providerJndi ).findAttribute() );
        members.add( new TemplateIntegerAttribute( providerJndi ).findAttribute() );

        return new AttributeCore( "0", getAttributeType(), new Compound( members ) );
    }
}
