package net.link.safeonline.attribute.provider.template;

import java.io.Serializable;
import java.util.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import net.link.safeonline.attribute.provider.*;
import net.link.safeonline.attribute.provider.exception.AttributeNotFoundException;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;
import net.link.safeonline.attribute.provider.template.panel.CustomAttributeInputPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Template {@link AttributeProvider}.
 * <p/>
 * Provides 2 attributes:
 * <p/>
 * <ul>
 * <p/>
 * <li>template.string : Random string, multivalued.</li>
 * <p/>
 * <li>template.boolean : Random boolean, multivalued.</li>
 * <p/>
 * <li>template.date : Random date, multivalued.</li>
 * <p/>
 * <li>template.double : Random double, multivalued.</li>
 * <p/>
 * <li>template.integer : Random integer, multivalued.</li>
 * <p/>
 * <li>template.integer : Random compound, containing all attribute types defined above, multivalued.</li>
 * <p/>
 * <li>template.unavailable : allways returns unavailable.</li>
 * <p/>
 * </ul>
 */
public class TemplateAttributeProvider extends AttributeProvider implements ServletContextListener {

    private static final Log LOG = LogFactory.getLog( TemplateAttributeProvider.class );

    private final List<TemplateAttribute> attributes = new LinkedList<TemplateAttribute>();

    public static final  String configGroup = "Template Attribute Provider";
    private static final String configSize  = "Size";
    private static final int    defaultSize = 3;

    public TemplateAttributeProvider() {

        attributes.add( new TemplateStringAttribute( getJndiLocation() ) );
        attributes.add( new TemplateBooleanAttribute( getJndiLocation() ) );
        attributes.add( new TemplateDateAttribute( getJndiLocation() ) );
        attributes.add( new TemplateDoubleAttribute( getJndiLocation() ) );
        attributes.add( new TemplateIntegerAttribute( getJndiLocation() ) );
        attributes.add( new TemplateCompoundAttribute( getJndiLocation() ) );
        attributes.add( new TemplateUnavailableAttribute( getJndiLocation() ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AttributeCore> listAttributes(final LinkIDService linkIDService, final String userId, final String attributeName,
                                              final boolean filterInvisible) {

        Integer size = (Integer) linkIDService.getConfigurationService().getConfigurationValue( configGroup, configSize, defaultSize );

        for (TemplateAttribute templateAttribute : attributes) {
            if (templateAttribute.getAttributeType().getName().equals( attributeName ))
                return templateAttribute.listAttributes( size );
        }

        throw new RuntimeException( "Attribute \"" + attributeName + "\" not provided by Template Attribute Provider" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeCore findAttribute(final LinkIDService linkIDService, final String userId, final String attributeName,
                                       final String attributeId) {

        for (TemplateAttribute templateAttribute : attributes) {
            if (templateAttribute.getAttributeType().getName().equals( attributeName ))
                return templateAttribute.findAttribute();
        }

        throw new RuntimeException( "Attribute \"" + attributeName + "\" not provided by Template Attribute Provider" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeCore findCompoundAttributeWhere(final LinkIDService linkIDService, final String userId,
                                                    final String parentAttributeName, final String memberAttributeName,
                                                    final Serializable memberValue) {

        throw new RuntimeException( "Attribute \"" + parentAttributeName + " \" is not compound." );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttributes(final LinkIDService linkIDService, final String userId, final String attributeName) {

        throw new RuntimeException( "Attribute \"" + attributeName + " \" is not editable" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(final LinkIDService linkIDService, final String userId, final String attributeName,
                                final String attributeId)
            throws AttributeNotFoundException {

        throw new RuntimeException( "Attribute \"" + attributeName + " \" is not editable" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttributes(final LinkIDService linkIDService, final String attributeName) {

        throw new RuntimeException( "Attribute \"" + attributeName + " \" is not editable" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeCore setAttribute(final LinkIDService linkIDService, final String userId, final AttributeCore attribute) {

        throw new RuntimeException( "Attribute \"" + attribute.getName() + " \" is not editable" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AttributeType> getSupportedAttributeTypes() {

        List<AttributeType> attributeTypes = new LinkedList<AttributeType>();
        for (TemplateAttribute templateAttribute : attributes) {
            attributeTypes.add( templateAttribute.getAttributeType() );
        }
        return attributeTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void intialize(final LinkIDService linkIDService) {

        linkIDService.getConfigurationService().initConfigurationValue( configGroup, configSize, defaultSize );
    }

    @Override
    public AttributeProvider getAttributeProvider() {

        return new TemplateAttributeProvider();
    }

    @Override
    public String getName() {

        return "Template";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Serializable, Long> categorize(final LinkIDService linkIDService, final List<String> subjects, final String attributeName) {

        return new HashMap<Serializable, Long>();
    }

    /**
     * {@inheritDoc}
     */
    public void contextInitialized(final ServletContextEvent sce) {

        LOG.debug( "bind Template attribute provider" );
        register();
    }

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(final ServletContextEvent sce) {

        LOG.debug( "unbind Template attribute provider" );
        unregister();
    }

    @Override
    public AttributeInputPanel getAttributeInputPanel(final LinkIDService linkIDService, final String id, final AttributeCore attribute) {

        if (attribute.getAttributeType().getType() == DataType.STRING && !attribute.isUnavailable()) {
            return new CustomAttributeInputPanel( id, attribute );
        } else
            return getDefaultAttributeInputPanel( id, attribute );
    }
}
