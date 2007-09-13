/*
 * SafeOnline project.
 * 
 * Copyright 2005-2007 Frank Cornelis H.S.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.validation.validator;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalog for validators. Flyweight pattern.
 * 
 * @author fcorneli
 * 
 */
public class ValidatorCatalog {

	private static Map<Class<? extends Validator>, Validator<?>> instances = new HashMap<Class<? extends Validator>, Validator<?>>();

	private ValidatorCatalog() {
		// empty
	}

	public static Validator<?> getInstance(
			Class<? extends Validator> validatorClass) {
		Validator<?> instance = ValidatorCatalog.instances.get(validatorClass);
		if (null == instance) {
			try {
				instance = validatorClass.newInstance();
				ValidatorCatalog.instances.put(validatorClass, instance);
			} catch (Exception e) {
				throw new IllegalStateException(
						"Unable to get instance of class: "
								+ validatorClass.getName());
			}
		}
		return instance;
	}
}
