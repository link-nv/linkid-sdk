/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.jacc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
public class BasicPolicyHandler<T> extends HashMap<String, T> implements
		PolicyContextHandler {

	private static final long serialVersionUID = 1L;

	private static final Map<Class<?>, BasicPolicyHandler<?>> handlers = new HashMap<Class<?>, BasicPolicyHandler<?>>();

	private List<String> keys;

	private Class<T> type;

	/**
	 * Make sure this constructor is private.
	 */
	private BasicPolicyHandler(Class<T> type, String... keys) {

		this.type = type;
		this.keys = Arrays.asList(keys);
	}

	/**
	 * @see #getContext(String, Object)
	 */
	public T getContext(String key) throws PolicyContextException {

		return getContext(key, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public T getContext(String key, @SuppressWarnings("unused")
	Object data) throws PolicyContextException {

		try {
			if (supports(key))
				return this.type.cast(PolicyContext.getContext(key));

			return null;
		}

		catch (ClassCastException e) {
			throw new PolicyContextException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getKeys() {

		return this.keys.toArray(new String[this.keys.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supports(String key) {

		return this.keys.contains(key);
	}

	/**
	 * Register an object in the active JACC Context.<br>
	 * {@inheritDoc}
	 */
	public boolean register(String key, T value) {

		try {
			if (!supports(key))
				return false;

			PolicyContext.registerHandler(key, this, true);
			put(key, value);

			return true;
		}

		catch (PolicyContextException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Get a policy handler that manages objects of the given class. This method
	 * makes sure there is only one handler for a certain type in the entire
	 * application.
	 */
	@SuppressWarnings("unchecked")
	public static <T> BasicPolicyHandler<T> getHandlerFor(Class<T> type,
			String... keys) {

		String[] handleKeys = keys;
		if (null == handleKeys || handleKeys.length == 0)
			handleKeys = new String[] { type.getClass().toString() };

		if (!handlers.containsKey(type))
			handlers.put(type, new BasicPolicyHandler<T>(type, handleKeys));

		return (BasicPolicyHandler<T>) handlers.get(type);
	}
}
