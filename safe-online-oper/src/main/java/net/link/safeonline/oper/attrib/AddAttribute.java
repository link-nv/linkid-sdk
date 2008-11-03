/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.attrib;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.entity.AttributeTypeEntity;


@Local
public interface AddAttribute {

    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "AddAttributeBean/local";

    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Accessors.
     */
    String getName();

    void setName(String name);

    String getNode();

    void setNode(String node);

    String getCategory();

    void setCategory(String category);

    String getType();

    void setType(String type);

    boolean isUserVisible();

    void setUserVisible(boolean userVisible);

    boolean isUserEditable();

    void setUserEditable(boolean userEditable);

    String getLocationOption();

    void setLocationOption(String locationOption);

    Long getCacheTimeout();

    void setCacheTimeout(Long cacheTimeout);

    String getPlugin();

    void setPlugin(String plugin);

    String getPluginConfiguration();

    void setPluginConfiguration(String configuration);

    List<AttributeTypeEntity> getSourceMemberAttributes();

    void setSourceMemberAttributes(List<AttributeTypeEntity> sourceMemberAttributes);

    List<AttributeTypeEntity> getTargetMemberAttributes();

    void setTargetMemberAttributes(List<AttributeTypeEntity> targetMemberAttributes);

    /*
     * Actions.
     */
    String next();

    String typeNext();

    String add() throws NodeNotFoundException, ExistingAttributeTypeException, AttributeTypeNotFoundException,
                AttributeTypeDefinitionException;

    String cancel();

    String membersNext();

    String membersAccessControlNext();

    String acNext();

    String locationNext();

    /*
     * Factory.
     */
    List<SelectItem> typesFactory();

    List<SelectItem> datatypesFactory();

    void memberAccessControlAttributesFactory();

    List<SelectItem> nodeFactory();

    List<SelectItem> locationTypesFactory();

    List<SelectItem> pluginFactory();
}
