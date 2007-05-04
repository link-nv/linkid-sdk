/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.prescription;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

@Local
public interface PrescriptionCreate {

	/*
	 * Factory.
	 */
	List<SelectItem> medicinesFactory();

	/*
	 * Accessors.
	 */
	List<String> getSelectedMedicines();

	void setSelectedMedicines(List<String> selectedMedicines);

	/*
	 * Lifecycle.
	 */
	void destroyCallback();

	/*
	 * Actions.
	 */
	String create();
}
