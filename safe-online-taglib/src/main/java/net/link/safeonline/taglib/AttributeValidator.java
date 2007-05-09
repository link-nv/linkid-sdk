/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.taglib;

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.AttributeDO;

/**
 * JSF validator for {@link AttributeDO}.
 * 
 * @author fcorneli
 * 
 */
public class AttributeValidator implements Validator {

	public static final String VALIDATOR_ID = "net.link.validator.attribute";

	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		UIInput inputComponent = (UIInput) component;
		boolean required = inputComponent.isRequired();
		if (false == required) {
			return;
		}
		AttributeDO attribute = (AttributeDO) value;
		String type = attribute.getType();
		TypeValidator typeValidator = typeValidators.get(type);
		if (null == typeValidator) {
			FacesMessage facesMessage = new FacesMessage("unsupported type: "
					+ type);
			throw new ValidatorException(facesMessage);
		}
		typeValidator.validate(context, attribute);
	}

	private interface TypeValidator {

		void validate(FacesContext context, AttributeDO attribute)
				throws ValidatorException;
	}

	private static class StringTypeValidator implements TypeValidator {

		public void validate(FacesContext context, AttributeDO attribute)
				throws ValidatorException {
			String value = attribute.getStringValue();
			if (null == value) {
				FacesMessage facesMessage = new FacesMessage(
						"string value is null");
				throw new ValidatorException(facesMessage);
			}
			if (0 == value.length()) {
				FacesMessage facesMessage = new FacesMessage(
						"string value is empty");
				throw new ValidatorException(facesMessage);
			}
		}
	}

	private static class BooleanTypeValidator implements TypeValidator {

		public void validate(FacesContext context, AttributeDO attribute)
				throws ValidatorException {
			Boolean value = attribute.getBooleanValue();
			if (null == value) {
				FacesMessage facesMessage = new FacesMessage(
						"boolean value is null");
				throw new ValidatorException(facesMessage);
			}
		}
	}

	private static final Map<String, TypeValidator> typeValidators = new HashMap<String, TypeValidator>();

	static {
		typeValidators.put(SafeOnlineConstants.STRING_TYPE,
				new StringTypeValidator());
		typeValidators.put(SafeOnlineConstants.BOOLEAN_TYPE,
				new BooleanTypeValidator());
	}
}
