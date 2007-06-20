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

	String getCategory();

	void setCategory(String category);

	String getType();

	void setType(String type);

	boolean isUserVisible();

	void setUserVisible(boolean userVisible);

	boolean isUserEditable();

	void setUserEditable(boolean userEditable);

	void setSelectedMemberAttributes(
			AttributeTypeEntity[] selectedMemberAttributes);

	AttributeTypeEntity[] getSelectedMemberAttributes();

	/*
	 * Actions.
	 */
	String next();

	String typeNext();

	String add();

	String cancel();

	String membersNext();

	String membersAccessControlNext();

	/*
	 * Factory.
	 */
	List<SelectItem> datatypesFactory();

	List<SelectItem> memberAttributesFactory();

	void memberAccessControlAttributesFactory();
}
