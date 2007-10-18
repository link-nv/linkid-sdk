/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.jacc;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author lhunath
 */
public class ProfileData extends HashMap<String, Long> {

	/**
	 * Just a string that is used to identify the profiler's data.<br>
	 * <br>
	 * It is used by the {@link ProfileInterceptor} to identify its lock onto
	 * the {@link javax.interceptor.InvocationContext} and by both the
	 * {@link ProfileInterceptor} and
	 * {@link net.link.safeonline.util.webapp.filter.ProfileFilter} to
	 * communicate {@link ProfileData} over the JACC Context.
	 */
	public static final String KEY = ProfileData.class.getName();

	/**
	 * The header used to identify the method for a profile entry.
	 */
	private static final String METHOD_HEADER = "X-Profiled-Header-";

	/**
	 * The header used to identify the time it took for a profile entry's method
	 * to execute.
	 */
	private static final String TIMING_HEADER = "X-Profiled-Duration-";

	/**
	 * Create a new {@link ProfileData} instance from scratch.
	 */
	public ProfileData() {

		super();
	}

	/**
	 * Create a new {@link ProfileData} instance by parsing the given headers
	 * for profile data.
	 */
	public ProfileData(Map<String, String> headers) {

		// Prepare the patterns to match our headers against.
		Pattern methodRegex = Pattern.compile(METHOD_HEADER + "(\\d+)");
		Pattern timingRegex = Pattern.compile(TIMING_HEADER + "(\\d+)");

		// We'll first collect data we care about in arrays.
		String[] methods = new String[headers.size()];
		Long[] timings = new Long[headers.size()];

		// Parse every header to see if it's a profiler header and extract
		// interesting data into the arrays.
		for (Map.Entry<String, String> header : headers.entrySet()) {
			Matcher methodMatcher = methodRegex.matcher(header.getKey());
			Matcher timingMatcher = timingRegex.matcher(header.getKey());

			if (methodMatcher.matches()) {
				int index = Integer.parseInt(methodMatcher.group(1));
				methods[index] = header.getValue();
			}

			else if (timingMatcher.matches()) {
				int index = Integer.parseInt(timingMatcher.group(1));
				timings[index] = Long.parseLong(header.getValue());
			}
		}

		// Now fill up our instance of ProfileData with the values we collected
		// in the arrays.
		for (int i = 0; i < headers.size(); ++i)
			put(methods[i], timings[i]);
	}

	public Map<String, String> getHeaders() {

		Map<String, String> headers = new HashMap<String, String>();
		int entry = 0;

		for (Map.Entry<String, Long> profileEntry : entrySet()) {
			String method = profileEntry.getKey();
			String duration = String.valueOf(profileEntry.getValue());

			headers.put(METHOD_HEADER + entry, method);
			headers.put(TIMING_HEADER + entry, duration);

			entry++;
		}

		return headers;
	}
}
