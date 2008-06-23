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

import net.link.safeonline.authentication.exception.AttributeTypeDefinitionException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.entity.AttributeTypeEntity;

@Local
public interface AddAttribute {

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

	boolean isDeviceAttribute();

	void setDeviceAttribute(boolean deviceAttribute);

	List<AttributeTypeEntity> getSourceMemberAttributes();

	void setSourceMemberAttributes(
			List<AttributeTypeEntity> sourceMemberAttributes);

	List<AttributeTypeEntity> getTargetMemberAttributes();

	void setTargetMemberAttributes(
			List<AttributeTypeEntity> targetMemberAttributes);

	/*
	 * Actions.
	 */
	String next();

	String typeNext();

	String add() throws NodeNotFoundException, ExistingAttributeTypeException,
			AttributeTypeNotFoundException, AttributeTypeDefinitionException;

	String cancel();

	String membersNext();

	String membersAccessControlNext();

	/*
	 * Factory.
	 */
	List<SelectItem> datatypesFactory();

	void memberAccessControlAttributesFactory();

	List<SelectItem> nodeFactory();
}
