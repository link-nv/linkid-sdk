/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

public enum HistoryEventType {
	LOGIN_SUCCESS("login_success"), LOGIN_PASSWORD_ATTRIBUTE_NOT_FOUND(
			"login_password_attribute_not_found"), LOGIN_INCORRECT_PASSWORD(
			"login_incorrect_password"), LOGIN_INCORRECT_MOBILE_TOKEN(
			"login_incorrect_mobile_token"), LOGIN_APPLICATION_NOT_FOUND(
			"login_application_not_found"), LOGIN_REGISTRATION(
			"login_registration"), SUBSCRIPTION_NOT_FOUND(
			"subscription_not_found"), SUBSCRIPTION_ADD("subscription_add"), SUBSCRIPTION_REMOVE(
			"subscription_remove"), ATTRIBUTE_CHANGE("attribute_change"), ATTRIBUTE_REMOVE(
			"attribute_remove"), ATTRIBUTE_ADD("attribute_add"), ATTRIBUTE_PROVIDER_CHANGE(
			"attribute_provider_change"), ATTRIBUTE_PROVIDER_REMOVE(
			"attribute_provider_remove"), ATTRIBUTE_PROVIDER_ADD(
			"attribute_provider_add"), IDENTITY_CONFIRMATION(
			"identity_confirmation"), HELPDESK_ID("helpdesk_id");

	private final String key;

	HistoryEventType(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
}
