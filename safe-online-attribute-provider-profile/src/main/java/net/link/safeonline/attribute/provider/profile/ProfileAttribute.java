package net.link.safeonline.attribute.provider.profile;

import java.io.Serializable;
import java.util.List;
import net.link.safeonline.attribute.provider.AttributeCore;
import net.link.safeonline.attribute.provider.AttributeType;
import net.link.safeonline.attribute.provider.exception.AttributeNotFoundException;
import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
import net.link.safeonline.attribute.provider.service.LinkIDService;
import org.jetbrains.annotations.Nullable;


public interface ProfileAttribute extends Serializable {

    AttributeType getAttributeType();

    List<AttributeCore> listAttributes(LinkIDService linkIDService, String userId);

    @Nullable
    AttributeCore findAttribute(LinkIDService linkIDService, String userId, String attributeId);

    void initialize(LinkIDService linkIDService);

    void removeAttributes(LinkIDService linkIDService, String userId);

    void removeAttribute(LinkIDService linkIDService, String userId, String attributeId)
            throws AttributeNotFoundException;

    void removeAttributes(LinkIDService linkIDService);

    AttributeCore setAttribute(LinkIDService linkIDService, String userId, AttributeCore attribute);

    @Nullable
    AttributeInputPanel findAttributeInputPanel(LinkIDService linkIDService, String id, String userId, AttributeCore attribute);

    String getName();
}
