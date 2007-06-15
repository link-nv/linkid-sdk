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

import net.link.safeonline.authentication.service.AttributeDO;
import net.link.safeonline.entity.DatatypeType;

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
		if (false == attribute.isRequired()) {
			/*
			 * In case of compounded member attributes the attribute can be
			 * optional.
			 */
			return;
		}
		DatatypeType type = attribute.getType();
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

	private static final Map<DatatypeType, TypeValidator> typeValidators = new HashMap<DatatypeType, TypeValidator>();

	static {
		typeValidators.put(DatatypeType.STRING, new StringTypeValidator());
		typeValidators.put(DatatypeType.BOOLEAN, new BooleanTypeValidator());
	}
}
