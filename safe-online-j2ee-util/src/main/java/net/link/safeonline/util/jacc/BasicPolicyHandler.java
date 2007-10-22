/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.jacc;

import java.util.HashMap;
import java.util.Map;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;

/**
 * This is a basic implementation of a JACC Policy Handler that can be used to
 * register data in the JACC Policy Context.<br>
 * <br>
 * This implementation is based on {@link HashMap} for storing the data that is
 * registered onto the policy context. Any data that is put into the map will be
 * transparantly registered onto the JACC Context and any data retrieved from
 * the JACC context will be retrieved from the backing map.<br>
 * <br>
 * This implementation supports generics allowing you to specify what type of
 * data you'd like to register onto the JACC Context.<br>
 * <br>
 * This implementation ignores the handler data object as it has no use for it.
 * 
 * @author mbillemo
 */
public class BasicPolicyHandler<T> extends HashMap<String, Map<String, T>>
		implements PolicyContextHandler {

	/**
	 * @see #getContext(String, Object)
	 */
	public T getContext(String key) {

		return getContext(key, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public T getContext(String key, Object data) {

		return getContext().get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getKeys() {

		return getContext().keySet().toArray(new String[size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supports(String key) {

		return getContext().containsKey(key);
	}

	/**
	 * Register an object in the active JACC Context.<br>
	 * {@inheritDoc}
	 */
	public T register(String key, T value) {

		try {
			PolicyContext.registerHandler(key, this, true);
			return getContext().put(key, value);
		}

		catch (PolicyContextException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Retrieve the active JACC Context (the context for the active thread).
	 */
	private Map<String, T> getContext() {

		// Obtain the current JACC Context's ID.
		// If none is set, set it to something unique to this thread (its name).
		String contextId = PolicyContext.getContextID();
		if (null == contextId)
			PolicyContext.setContextID(contextId = Thread.currentThread()
					.getName());

		// Create a backing hashmap for this context if there is none yet.
		if (!containsKey(contextId))
			put(contextId, new HashMap<String, T>());

		return get(contextId);
	}
}
